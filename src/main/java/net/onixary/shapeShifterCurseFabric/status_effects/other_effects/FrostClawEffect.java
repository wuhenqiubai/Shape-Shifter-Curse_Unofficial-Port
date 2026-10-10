package net.onixary.shapeShifterCurseFabric.status_effects.other_effects;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class FrostClawEffect extends MobEffect {
    public FrostClawEffect() { super(MobEffectCategory.BENEFICIAL, 0xB9EDFF); }

    @Override public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) { return true; }
    // ⚠ 1.21 起 MobEffect#applyEffectTick 返回 boolean：返回 false 会移除该效果实例。
    //   霜爪是持续效果（shouldApplyEffectTickThisTick 恒 true），必须返回 true 维持。
    @Override public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        freeze(entity, entity.position());
        return true;
    }

    public static void freeze(LivingEntity entity, Vec3 center) {
        Level world = entity.level();
        if (world.isClientSide) return;
        BlockState ice = Blocks.FROSTED_ICE.defaultBlockState();
        BlockPos origin = BlockPos.containing(center);
        // Vanilla level I: radius 2 + 1, exposed source water, scheduled melting.
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-3, -1, -3), origin.offset(3, -1, 3))) {
            if (pos.closerToCenterThan(center, 3) && world.getBlockState(pos.above()).isAir()
                    && world.getBlockState(pos) == FrostedIceBlock.meltsInto()
                    && ice.canSurvive(world, pos) && world.isUnobstructed(ice, pos, CollisionContext.empty())
                    && world.getWorldBorder().isWithinBounds(pos)) {
                world.setBlockAndUpdate(pos, ice);
                world.scheduleTick(pos, Blocks.FROSTED_ICE, Mth.nextInt(entity.getRandom(), 60, 120));
            }
        }
    }

    public static void beforeMove(LivingEntity entity, Vec3 movement) {
        // TODO 这个得优化一下运行速度 这AI怎么老整这种卡的要命的玩意
        if (entity.level().isClientSide) return;
        freeze(entity, entity.position());
        if (movement.y >= 0) return;
        movement = Entity.collideBoundingBox(entity, movement, entity.getBoundingBox(),
                entity.level(), entity.level().getEntityCollisions(entity, entity.getBoundingBox().expandTowards(movement)));
        int steps = Math.min(64, Mth.ceil(movement.length() * 2));
        for (int i = 1; i <= steps; i++) {
            freeze(entity, entity.position().add(movement.scale((double) i / steps)));
        }
    }
}

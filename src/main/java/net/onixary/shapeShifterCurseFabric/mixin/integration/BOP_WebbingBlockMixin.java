package net.onixary.shapeShifterCurseFabric.mixin.integration;

import biomesoplenty.block.WebbingBlock;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.onixary.shapeShifterCurseFabric.additional_power.SlowdownPercentPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * BiomesO' Plenty 兼容层（重写自上游 Forge 预编译 BOP_WebbingBlockMixin.class）。
 * <p>
 * BOP 的 Webbing 方块原生把实体速度乘 (0.625, 0.75, 0.625)；这里改为按玩家身上
 * {@link SlowdownPercentPower} 的倍率来减速（没有该 power 时倍率 1.0，效果与原版一致），
 * 并 cancel 掉 BOP 的原始逻辑避免叠加减速。
 * <p>
 * 从上游 1.20.1 Yarn 版移植：{@code World→Level}、{@code PlayerEntity→Player}、
 * {@code getVelocity/setVelocity → getDeltaMovement/setDeltaMovement}，
 * 注入点 {@code onEntityCollision → entityInside}（1.21.1 签名未变，仍是
 * {@code (BlockState, Level, BlockPos, Entity)}，在 BOP 里是 public 的 override）。
 * 仅在 BOP 存在时经 MixinConfigPlugin 条件注入。
 */
@Mixin(WebbingBlock.class)
public class BOP_WebbingBlockMixin {
    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    private void ssc$entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean b, CallbackInfo ci) {
        if (entity instanceof Player player) {
            List<SlowdownPercentPower> slowdownPower = PowerHolderComponent.getPowers(player, SlowdownPercentPower.class);
            float slowdownPercent = 1.0f;
            for (SlowdownPercentPower power : slowdownPower) {
                slowdownPercent *= power.Multiplier;
            }
            player.setDeltaMovement(player.getDeltaMovement().multiply(
                    1D - (0.375D * slowdownPercent),
                    1D - (0.25D * slowdownPercent),
                    1D - (0.375D * slowdownPercent)));
            ci.cancel();
        }
    }
}

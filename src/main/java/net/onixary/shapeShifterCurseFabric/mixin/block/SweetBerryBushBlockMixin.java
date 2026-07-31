package net.onixary.shapeShifterCurseFabric.mixin.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.additional_power.PreventBerryEffectPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 修改甜浆果丛方块行为
@Mixin(value = SweetBerryBushBlock.class, priority = 1001)
public abstract class SweetBerryBushBlockMixin {
    // 1.21.11: entityInside 增加 InsideBlockEffectApplier/boolean 参数；伤害调用从 hurt 改为 hurtServer
    @Inject(
            method = "entityInside",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
            cancellable = true
    )
    private void preventBerryDamage(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier applier, boolean bl, CallbackInfo ci) {
        if (entity instanceof Player player) {
            if (PowerHolderComponent.hasPower(player, PreventBerryEffectPower.class)) {
                ci.cancel();
            }
        }
    }

    @WrapOperation(
            method = "entityInside",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;makeStuckInBlock(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/phys/Vec3;)V"
            )
    )
    private void preventBerrySlowdown(Entity entity, BlockState state, Vec3 multiplier, Operation<Void> original) {
        // 如果是玩家则跳过减速
        if ((entity instanceof Player)) {
            if (!PowerHolderComponent.hasPower((Player)entity, PreventBerryEffectPower.class)) {
                original.call(entity, state, multiplier);
            }
        }
        else{
            original.call(entity, state, multiplier);
        }
    }
}


package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.onixary.shapeShifterCurseFabric.additional_power.WitherIntervalPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MobEffectInstance.class)
public abstract class WitherIntervalMixin {
    @Redirect(method = "tickServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/lang/Runnable;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffect;shouldApplyEffectTickThisTick(II)Z"))
    private boolean ssc$witherInterval(MobEffect effect, int duration, int amplifier, ServerLevel serverLevel, LivingEntity entity, Runnable callback) {
        boolean vanilla = effect.shouldApplyEffectTickThisTick(duration, amplifier);
        return effect == MobEffects.WITHER.value() ? WitherIntervalPower.shouldApply(entity, duration, amplifier, vanilla) : vanilla;
    }
}

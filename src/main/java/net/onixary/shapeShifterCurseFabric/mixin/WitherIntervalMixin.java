package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.onixary.shapeShifterCurseFabric.additional_power.WitherIntervalPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MobEffectInstance.class)
public abstract class WitherIntervalMixin {
    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffect;shouldApplyEffectTickThisTick(II)Z"))
    private boolean ssc$witherInterval(MobEffect effect, int duration, int amplifier, LivingEntity entity, Runnable callback) {
        boolean vanilla = effect.shouldApplyEffectTickThisTick(duration, amplifier);
        return effect == MobEffects.WITHER ? WitherIntervalPower.shouldApply(entity, duration, amplifier, vanilla) : vanilla;
    }
}

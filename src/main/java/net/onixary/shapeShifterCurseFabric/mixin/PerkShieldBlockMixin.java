package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.onixary.shapeShifterCurseFabric.additional_power.ActionOnShieldBlockPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class PerkShieldBlockMixin {
    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurtCurrentlyUsedShield(F)V"))
    private void ssc$shieldResponse(DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        ActionOnShieldBlockPower.onBlock((LivingEntity) (Object) this, damageSource);
    }
}

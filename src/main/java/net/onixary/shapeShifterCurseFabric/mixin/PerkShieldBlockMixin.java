package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.onixary.shapeShifterCurseFabric.additional_power.ActionOnShieldBlockPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class PerkShieldBlockMixin {
    @Inject(method = "applyItemBlocking",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/BlocksAttacks;hurtBlockingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;F)V"))
    private void ssc$shieldResponse(ServerLevel serverLevel, DamageSource damageSource, float f, CallbackInfoReturnable<Float> cir) {
        ActionOnShieldBlockPower.onBlock((LivingEntity) (Object) this, damageSource);
    }
}

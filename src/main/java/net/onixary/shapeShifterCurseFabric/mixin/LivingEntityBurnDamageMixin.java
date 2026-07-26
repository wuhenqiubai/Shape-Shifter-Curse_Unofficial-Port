package net.onixary.shapeShifterCurseFabric.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.onixary.shapeShifterCurseFabric.additional_power.BurnDamageModifierPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(LivingEntity.class)
public class LivingEntityBurnDamageMixin {

    @ModifyArg(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V"), index = 1)
    private float modifyBurnDamage(DamageSource source, float amount) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.isOnFire() && (source.is(DamageTypeTags.IS_FIRE)
                && !entity.hasEffect(MobEffects.FIRE_RESISTANCE)))
        {
            List<BurnDamageModifierPower> powers = PowerHolderComponent.getPowers(entity, BurnDamageModifierPower.class);
            float totalModifier = powers
                    .stream()
                    .map(BurnDamageModifierPower::getDamageModifier)
                    .reduce(0f, Float::sum);

            powers.forEach(power -> {
                if (power.isActive()) {
                    power.executeAction(entity);
                }
            });

            return amount + totalModifier;
        }
        return amount;
    }
}

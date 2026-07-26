package net.onixary.shapeShifterCurseFabric.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.onixary.shapeShifterCurseFabric.additional_power.ModifyInstantDamagePower;
import net.onixary.shapeShifterCurseFabric.additional_power.ModifyInstantHealthPower;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(MobEffect.class)
public class StatusEffectMixin {
    @Unique
    private boolean applyEffect(MobEffect realThis, LivingEntity entity, @Nullable Entity source, @Nullable Entity attacker, int amplifier, double proximity, boolean IsInstantEffect) {
        // 返回值是是否覆盖了原版效果 如果返回false 还会执行原版效果
        int EffectValue = 4;
        float FinalValue;
        if (realThis == MobEffects.HEAL) {
            List<ModifyInstantHealthPower> PowerList = PowerHolderComponent.getPowers(entity, ModifyInstantHealthPower.class);
            if (PowerList.isEmpty()) {
                return false;
            }
            if (entity != null && entity.getType().is(EntityTypeTags.UNDEAD)) {
                EffectValue = -6;
            }
            FinalValue = (float) (EffectValue << amplifier);
            if (IsInstantEffect) {
                FinalValue = (float) (proximity * FinalValue + 0.5);
            }
            for (ModifyInstantHealthPower power : PowerList) {
                FinalValue = power.ApplyMulScale(FinalValue);
            }
            applyEffectDamage(entity, source, attacker, IsInstantEffect, FinalValue);
            return true;
        }
        if (realThis == MobEffects.HARM)  {
            List<ModifyInstantDamagePower> PowerList = PowerHolderComponent.getPowers(entity, ModifyInstantDamagePower.class);
            if (PowerList.isEmpty()) {
                return false;
            }
            EffectValue = -4;
            if (entity.getType().is(EntityTypeTags.UNDEAD)) {
                EffectValue = 6;
            }
            FinalValue = (float) (EffectValue << amplifier);
            if (IsInstantEffect) {
                FinalValue = (float) (proximity * FinalValue + 0.5);
            }
            for (ModifyInstantDamagePower power : PowerList) {
                FinalValue = power.ApplyMulScale(FinalValue);
            }
            applyEffectDamage(entity, source, attacker, IsInstantEffect, FinalValue);
            return true;
        }
        return false;
    }

    @Unique
    private void applyEffectDamage(LivingEntity entity, @Nullable Entity source, @Nullable Entity attacker, boolean IsInstantEffect, float finalValue) {
        if (finalValue == 0.0f) {
            return;
        }
        else if (finalValue > 0.0f) {
            entity.heal(finalValue);
        } else {
            DamageSource damageSource = entity.damageSources().magic();
            if (IsInstantEffect && (source != null && attacker != null)) {
                damageSource = entity.damageSources().indirectMagic(source, attacker);
            }
            entity.hurt(damageSource, -finalValue);
        }
    }

    @Inject(method = "applyEffectTick", at = @At("HEAD"), cancellable = true)
    private void applyUpdateEffect(LivingEntity entity, int amplifier, CallbackInfoReturnable<Boolean> cir) {
        MobEffect realThis = (MobEffect)(Object)this;
        if (applyEffect(realThis, entity, null, null, amplifier, 0.0, false)) {
            cir.cancel();
        }
    }

    @Inject(method = "applyInstantenousEffect", at = @At("HEAD"), cancellable = true)
    private void applyInstantEffect(Entity source, Entity attacker, LivingEntity target, int amplifier, double proximity, CallbackInfo ci) {
        MobEffect realThis = (MobEffect)(Object)this;
        if (applyEffect(realThis, target, source, attacker, amplifier, proximity, true)) {
            ci.cancel();
        }
    }
}
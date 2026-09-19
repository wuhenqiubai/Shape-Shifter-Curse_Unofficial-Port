package io.github.apace100.apoli.mixin.legacy;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.legacy.ModifyBehaviorPower;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(TargetingConditions.class)
public abstract class TargetingConditionsMixin {
    @Shadow
    @Final
    private boolean isCombat;

    @Inject(method = "test", at = @At("HEAD"), cancellable = true)
    private void origins_legacy$avoidCombatTargetingIfPassive(LivingEntity attacker, LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if (this.isCombat && attacker != null && target != null && attacker != target) {
            List<ModifyBehaviorPower> powers = PowerHolderComponent.getPowers(target, ModifyBehaviorPower.class);
            powers.removeIf(power -> !power.doesApply(attacker));

            if (!powers.isEmpty()) {
                for (ModifyBehaviorPower power : powers) {
                    if (power.getBehaviorType() == ModifyBehaviorPower.BehaviorType.PASSIVE) {
                        cir.setReturnValue(false);
                    }
                }
            }
        }
    }
}

package io.github.apace100.apoli.mixin.legacy;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.legacy.ModifyBehaviorPower;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(NearestAttackableTargetGoal.class)
public abstract class NearestAttackableTargetGoalMixin extends TargetGoal {
    @Shadow
    @Nullable
    protected LivingEntity target;

    public NearestAttackableTargetGoalMixin(Mob mob, boolean mustSee) {
        super(mob, mustSee);
    }

    @Inject(method = "findTarget", at = @At("TAIL"))
    private void origins_legacy$disableTargetingIfNotHostile(CallbackInfo ci) {
        if (this.target != null) {
            Mob self = this.mob;
            List<ModifyBehaviorPower> powers = PowerHolderComponent.getPowers(this.target, ModifyBehaviorPower.class);
            if (powers.isEmpty()) return;

            powers.removeIf(power -> !power.doesApply(self));

            if (!powers.isEmpty()) {
                for (ModifyBehaviorPower power : powers) {
                    if (power.getBehaviorType() != ModifyBehaviorPower.BehaviorType.HOSTILE) {
                        this.target = null;
                        break;
                    }
                }
            }
        }
    }
}

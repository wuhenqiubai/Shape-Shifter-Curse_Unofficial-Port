package io.github.apace100.apoli.mixin.legacy;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.legacy.ModifyBehaviorPower;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity {
    @Shadow
    @Final
    protected GoalSelector targetSelector;

    @Shadow
    @Nullable
    public abstract LivingEntity getTarget();

    @Shadow
    public abstract void setTarget(@Nullable LivingEntity target);

    protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;registerGoals()V", shift = At.Shift.AFTER))
    private void origins_legacy$addCustomAttackableGoal(EntityType<?> entityType, Level level, CallbackInfo ci) {
        Mob self = (Mob) (Object) this;

        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(self, LivingEntity.class, true, entity -> {
            List<ModifyBehaviorPower> powers = PowerHolderComponent.getPowers(entity, ModifyBehaviorPower.class);
            powers.removeIf(power -> !power.doesApply(self));

            if (!powers.isEmpty()) {
                for (ModifyBehaviorPower power : powers) {
                    if (power.getBehaviorType() == ModifyBehaviorPower.BehaviorType.HOSTILE) {
                        return true;
                    }
                }
            }

            return false;
        }));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void origins_legacy$removeTargetIfPassive(CallbackInfo ci) {
        LivingEntity target = this.getTarget();

        if (target != null) {
            Mob self = (Mob) (Object) this;
            List<ModifyBehaviorPower> powers = PowerHolderComponent.getPowers(target, ModifyBehaviorPower.class);
            powers.removeIf(power -> !power.doesApply(self));

            if (!powers.isEmpty()) {
                for (ModifyBehaviorPower power : powers) {
                    if (power.getBehaviorType() == ModifyBehaviorPower.BehaviorType.PASSIVE) {
                        this.setTarget(null);
                    }
                }
            }
        }
    }
}

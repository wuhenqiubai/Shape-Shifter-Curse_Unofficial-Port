package net.onixary.shapeShifterCurseFabric.mixin.mob;

import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.additional_power.AdditionalPowers;
import net.onixary.shapeShifterCurseFabric.mixin.accessor.MobEntityAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.function.Predicate;

@Mixin(Creeper.class)
public abstract class ScareCreeperMixin {

    @Inject(at = @At("TAIL"), method = "registerGoals")
    private void addGoals(CallbackInfo info) {
        GoalSelector goalSelector = ((MobEntityAccessor) this).getGoalSelector();
        GoalSelector targetSelector = ((MobEntityAccessor) this).getTargetSelector();
        Goal goal = new AvoidEntityGoal<>((Creeper) (Object) this, Player.class, AdditionalPowers.SCARE_CREEPERS::isActive, 3.0F, 1.0D, 1.2D, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
        goalSelector.addGoal(3, goal);
        Set<WrappedGoal> goals = targetSelector.getAvailableGoals();
        for (WrappedGoal prioritizedGoal : goals) {
            if (prioritizedGoal.getGoal() instanceof NearestAttackableTargetGoal<?> atg && prioritizedGoal.getPriority() == 1 && atg.targetType == Player.class) {
                Predicate<LivingEntity> targetPredicate = atg.targetConditions.selector;
                if (targetPredicate == null) {
                    targetPredicate = e -> !AdditionalPowers.SCARE_CREEPERS.isActive(e);
                } else {
                    targetPredicate = targetPredicate.and(e -> !AdditionalPowers.SCARE_CREEPERS.isActive(e));
                }
                atg.targetConditions.selector(targetPredicate);
            }
        }
    }
}

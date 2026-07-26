package net.onixary.shapeShifterCurseFabric.mixin.mob;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.additional_power.AdditionalPowers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.function.Predicate;

@Mixin(Spider.class)
public class SpiderEntityMixin extends Monster {
    protected SpiderEntityMixin(EntityType<? extends Monster> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(at = @At("TAIL"), method = "registerGoals")
    private void addGoals(CallbackInfo info) {
        Set<WrappedGoal> goals = this.targetSelector.getAvailableGoals();
        for (WrappedGoal prioritizedGoal : goals) {
            if (prioritizedGoal.getGoal() instanceof NearestAttackableTargetGoal<?> atg && prioritizedGoal.getPriority() == 2 && atg.targetType == Player.class) {
                Predicate<LivingEntity> targetPredicate = atg.targetConditions.selector;
                if (targetPredicate == null) {
                    targetPredicate = e -> !AdditionalPowers.SPIDER_FRIENDLY.isActive(e);
                } else {
                    targetPredicate = targetPredicate.and(e -> !AdditionalPowers.SPIDER_FRIENDLY.isActive(e));
                }
                atg.targetConditions.selector(targetPredicate);
            }
        }
    }
}
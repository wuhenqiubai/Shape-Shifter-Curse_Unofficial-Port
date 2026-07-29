package net.onixary.shapeShifterCurseFabric.mixin.mob;

import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.additional_power.AdditionalPowers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSkeleton.class)
public abstract class ScareSkeletonMixin extends Monster implements RangedAttackMob {
    protected ScareSkeletonMixin(EntityType<? extends Monster> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(at = @At("TAIL"), method = "registerGoals")
    private void addGoals(CallbackInfo info) {
        Goal goal = new AvoidEntityGoal<>(this, Player.class, AdditionalPowers.SCARE_SKELETON::isActive, 3.0F, 1.0D, 1.2D, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
        this.goalSelector.addGoal(3, goal);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/world/entity/ai/goal/Goal;)V", ordinal = 7), method = "registerGoals")
    private void redirectTargetGoal(GoalSelector goalSelector, int priority, Goal goal) {
	    Goal newGoal = new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, e -> !AdditionalPowers.SCARE_SKELETON.isActive(e));
        goalSelector.addGoal(priority, newGoal);
    }
}
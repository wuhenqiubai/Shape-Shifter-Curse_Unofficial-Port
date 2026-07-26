package net.onixary.shapeShifterCurseFabric.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class ActiveTargetGoalWithCondition<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    public Predicate<Mob> condition;

    public ActiveTargetGoalWithCondition(Mob mob, Class<T> targetClass, boolean checkVisibility, Predicate<Mob> condition) {
        super(mob, targetClass, checkVisibility);
        this.condition = condition;
    }

    public ActiveTargetGoalWithCondition(Mob mob, Class<T> targetClass, boolean checkVisibility, Predicate targetPredicate, Predicate<Mob> condition) {
        super(mob, targetClass, checkVisibility, targetPredicate);
        this.condition = condition;
    }

    public ActiveTargetGoalWithCondition(Mob mob, Class<T> targetClass, boolean checkVisibility, boolean checkCanNavigate, Predicate<Mob> condition) {
        super(mob, targetClass, checkVisibility, checkCanNavigate);
        this.condition = condition;
    }

    public ActiveTargetGoalWithCondition(Mob mob, Class<T> targetClass, int reciprocalChance, boolean checkVisibility, boolean checkCanNavigate, @Nullable Predicate<LivingEntity> targetPredicate, Predicate<Mob> condition) {
        super(mob, targetClass, reciprocalChance, checkVisibility, checkCanNavigate, targetPredicate);
        this.condition = condition;
    }

    @Override
    public boolean canUse() {
        if (this.condition.test(this.mob)) {
            return super.canUse();
        }
        return false;
    }
}
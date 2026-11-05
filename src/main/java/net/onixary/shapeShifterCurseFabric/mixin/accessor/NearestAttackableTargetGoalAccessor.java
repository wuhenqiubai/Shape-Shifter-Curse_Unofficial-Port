package net.onixary.shapeShifterCurseFabric.mixin.accessor;

import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NearestAttackableTargetGoal.class)
public interface NearestAttackableTargetGoalAccessor {
    @Accessor("targetType")
    Class<?> ssc_getTargetType();

    @Accessor("targetConditions")
    TargetingConditions ssc_getTargetConditions();
}

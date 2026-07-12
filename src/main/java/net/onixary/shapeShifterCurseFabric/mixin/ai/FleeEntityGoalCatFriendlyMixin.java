package net.onixary.shapeShifterCurseFabric.mixin.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.onixary.shapeShifterCurseFabric.additional_power.AdditionalPowers;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FleeEntityGoal.class)
public abstract class FleeEntityGoalCatFriendlyMixin<T extends LivingEntity> {
    @Shadow @Final protected PathAwareEntity mob;
    @Shadow @Nullable protected T targetEntity;

    @Inject(method = "canStart", at = @At("RETURN"), cancellable = true)
    private void ssc$modifyCanStart(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && this.targetEntity instanceof PlayerEntity player) {
            if ((this.mob instanceof OcelotEntity || this.mob instanceof CatEntity)
                && AdditionalPowers.CAT_FRIENDLY.isActive(player)) {
                cir.setReturnValue(false);
            }
        }
    }
}

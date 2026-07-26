package net.onixary.shapeShifterCurseFabric.mixin.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.additional_power.AdditionalPowers;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AvoidEntityGoal.class)
public abstract class FleeEntityGoalCatFriendlyMixin<T extends LivingEntity> {
    @Shadow @Final protected PathfinderMob mob;
    @Unique @Nullable protected T target;

    @Inject(method = "canUse", at = @At("RETURN"), cancellable = true)
    private void ssc$modifycanUse(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && this.target instanceof Player player) {
            if ((this.mob instanceof Ocelot || this.mob instanceof Cat)
                && AdditionalPowers.CAT_FRIENDLY.isActive(player)) {
                cir.setReturnValue(false);
            }
        }
    }
}
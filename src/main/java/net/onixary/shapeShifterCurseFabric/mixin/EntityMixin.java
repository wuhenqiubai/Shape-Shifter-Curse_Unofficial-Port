package net.onixary.shapeShifterCurseFabric.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.additional_power.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "isSteppingCarefully", at = @At("RETURN"), cancellable = true)
    private void bypassesSteppingEffects(CallbackInfoReturnable<Boolean> cir) {
        // 只有玩家需要这个Power 所以为了减少性能开销 所以做了个判断
        if ((Object)this instanceof Player player && !cir.getReturnValueZ()) {
            int powerCount = PowerHolderComponent.getPowers(player, BypassesSteppingEffectsPower.class).size();
            if (powerCount > 0) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "isSuppressingBounce", at = @At("RETURN"), cancellable = true)
    private void bypassesLandingEffects(CallbackInfoReturnable<Boolean> cir) {
        if ((Object)this instanceof Player player && !cir.getReturnValueZ()) {
            int powerCount = PowerHolderComponent.getPowers(player, BypassesLandingEffectsPower.class).size();
            if (powerCount > 0) {
                cir.setReturnValue(true);
            }
        }
    }

    @Unique
    private boolean shapeShifter$longStepInterval;

    @Inject(method = "nextStep", at = @At("RETURN"), cancellable = true)
    private void modifyFootstepSoundDistance(CallbackInfoReturnable<Float> cir) {
        if ((Object)this instanceof Player player) {
            var powers = PowerHolderComponent.getPowers(player, ModifyFootstepSoundSpeedPower.class);
            if (!powers.isEmpty()) {
                var power = powers.get(0);
                if (player.isInWater()) {
                    // This vanilla threshold also schedules swimming sounds; retain their uniform cadence.
                    shapeShifter$longStepInterval = false;
                    cir.setReturnValue(player.moveDist + 1.0f / power.getSpeedMultiplierFor(player));
                } else {
                    cir.setReturnValue(player.moveDist + power.getStepSoundDistance(player, 1.0f, shapeShifter$longStepInterval));
                    shapeShifter$longStepInterval = !shapeShifter$longStepInterval;
                }
            } else {
                shapeShifter$longStepInterval = false;
            }
        }
    }

    @Inject(method = "canFreeze", at = @At("HEAD"), cancellable = true)
    private void canFreeze(CallbackInfoReturnable<Boolean> cir) {
        if (PowerHolderComponent.hasPower((Entity) (Object) this, ImmuneFreezePower.class)) {
            cir.setReturnValue(false);
        }
    }

    private boolean lastIsSwimming = false;

    @Inject(method = "updateSwimming", at = @At("HEAD"))
    private void updateSwimmingH(CallbackInfo ci) {
        lastIsSwimming = ((Entity) (Object) this).isSwimming();
    }

    @Inject(method = "updateSwimming", at = @At("TAIL"))
    private void updateSwimmingT(CallbackInfo ci) {
        if (((Entity) (Object) this).isSwimming()) {
            return;
        }
        if ((Object) this instanceof Player player && PowerHolderComponent.hasPower(player, AlwaysSprintSwimmingPower.class)) {
            if (lastIsSwimming) {
                player.setSwimming(player.isInWater() && !player.isPassenger());
            } else {
                player.setSwimming(player.isUnderWater() && !player.isPassenger() && player.level().getFluidState(player.blockPosition()).is(FluidTags.WATER));
            }
        }
    }
}

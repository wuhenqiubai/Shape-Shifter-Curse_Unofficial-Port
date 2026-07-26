package net.onixary.shapeShifterCurseFabric.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.onixary.shapeShifterCurseFabric.additional_power.HissPhantomPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.entity.monster.Phantom$PhantomSweepAttackGoal")
public class HissPhantomMixin {
    @Unique
    private Phantom phantomEntity;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void setPhantomEntity(Phantom phantomEntity, CallbackInfo ci) {
        this.phantomEntity = phantomEntity;
    }

    @Inject(method = "canContinueToUse", at = @At("RETURN"), cancellable = true)
    private void shouldContinue(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity livingEntity = phantomEntity.getTarget();
        if (cir.getReturnValueZ()) {
            HissPhantomPower power = PowerHolderComponent.getPowers(livingEntity, HissPhantomPower.class).stream().findFirst().orElse(null);
            if (power != null && power.isActive()) {
                power.invokeAction(livingEntity, phantomEntity);
                cir.setReturnValue(false);
            }
        }
    }
}

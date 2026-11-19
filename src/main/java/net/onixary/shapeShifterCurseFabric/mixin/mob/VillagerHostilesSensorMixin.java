package net.onixary.shapeShifterCurseFabric.mixin.mob;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.VillagerHostilesSensor;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.additional_power.ScareVillagerPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerHostilesSensor.class)
public abstract class VillagerHostilesSensorMixin {
    @Inject(method = "isClose", at = @At("HEAD"), cancellable = true)
    private void isCloseEnoughForDanger(LivingEntity body, LivingEntity mob, CallbackInfoReturnable<Boolean> callback) {
        if (mob instanceof Player) {
            if (PowerHolderComponent.hasPower(mob, ScareVillagerPower.class)) {
                callback.setReturnValue(mob.distanceTo(body) <= 8.0);
            }
        }
    }

    @Inject(method = "isHostile", at = @At("HEAD"), cancellable = true)
    private void isHostile(LivingEntity entity, CallbackInfoReturnable<Boolean> callback) {
        if (entity instanceof Player) {
            if (PowerHolderComponent.hasPower(entity, ScareVillagerPower.class)) {
                callback.setReturnValue(true);
            }
        }
    }
}

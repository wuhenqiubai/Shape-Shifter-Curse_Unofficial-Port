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
    private void isCloseEnoughForDanger(LivingEntity villager, LivingEntity hostile, CallbackInfoReturnable<Boolean> callback) {
        if (hostile instanceof Player) {
            if (PowerHolderComponent.hasPower(hostile, ScareVillagerPower.class)) {
                callback.setReturnValue(hostile.distanceTo(villager) <= 8.0);
            }
        }
    }

    @Inject(method = "isHostile", at = @At("HEAD"), cancellable = true)
    private void isHostile(LivingEntity hostile, CallbackInfoReturnable<Boolean> callback) {
        if (hostile instanceof Player) {
            if (PowerHolderComponent.hasPower(hostile, ScareVillagerPower.class)) {
                callback.setReturnValue(true);
            }
        }
    }
}

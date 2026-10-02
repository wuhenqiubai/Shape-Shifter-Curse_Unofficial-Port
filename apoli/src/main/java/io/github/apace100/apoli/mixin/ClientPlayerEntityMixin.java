package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.authlib.GameProfile;
import io.github.apace100.apoli.access.WaterMovingEntity;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.IgnoreWaterPower;
import io.github.apace100.apoli.power.ModifyAirSpeedPower;
import io.github.apace100.apoli.power.PreventSprintingPower;
import io.github.apace100.apoli.power.SwimmingPower;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayer implements WaterMovingEntity {

    private boolean isMoving = false;

    public ClientPlayerEntityMixin(ClientLevel world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(at = @At("HEAD"), method = "isUnderWater", cancellable = true)
    private void allowSwimming(CallbackInfoReturnable<Boolean> cir)  {
        if(PowerHolderComponent.hasPower(this, SwimmingPower.class)) {
            cir.setReturnValue(true);
        } else if(PowerHolderComponent.hasPower(this, IgnoreWaterPower.class)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(at = @At("HEAD"), method = "aiStep")
    private void beginMovementPhase(CallbackInfo ci) {
        isMoving = true;
    }

    @Inject(at = @At("TAIL"), method = "aiStep")
    private void endMovementPhase(CallbackInfo ci) {
        isMoving = false;
    }

    public boolean isInMovementPhase() {
        return isMoving;
    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Abilities;getFlyingSpeed()F"))
    private float modifyFlySpeed(float original){
        return PowerHolderComponent.modify(this, ModifyAirSpeedPower.class, original);
    }

    @WrapWithCondition(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;setSprinting(Z)V"))
    private boolean modifySprintAbility(LocalPlayer instance, boolean original) {
        if (original) {
            boolean prevent = PowerHolderComponent.hasPower(this, PreventSprintingPower.class);
            return !prevent;
        }

        return true;
    }
}

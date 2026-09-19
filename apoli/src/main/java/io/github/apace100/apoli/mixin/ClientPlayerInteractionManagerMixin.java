package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ActionOnBlockUsePower;
import io.github.apace100.apoli.power.PreventBlockUsePower;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Collectors;

@Mixin(MultiPlayerGameMode.class)
public abstract class ClientPlayerInteractionManagerMixin {

    @Shadow protected abstract void startPrediction(ClientLevel world, PredictiveAction packetCreator);

    @Inject(method = "performUseItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSecondaryUseActive()Z"), cancellable = true)
    private void preventBlockInteraction(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if(PowerHolderComponent.getPowers(player, PreventBlockUsePower.class).stream().anyMatch(p -> p.doesPrevent(player.level(), hitResult.getBlockPos()))) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "performUseItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSecondaryUseActive()Z", shift = At.Shift.AFTER), cancellable = true)
    private void executeBlockUseActions(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        InteractionResult result = InteractionResult.PASS;
        for(ActionOnBlockUsePower p : PowerHolderComponent.getPowers(player, ActionOnBlockUsePower.class).stream()
            .filter(p -> p.shouldExecute(hitResult.getBlockPos(), hitResult.getDirection(), hand, player.getItemInHand(hand))).collect(Collectors.toList())) {
            InteractionResult ar = p.executeAction(hitResult.getBlockPos(), hitResult.getDirection(), hand);
            if(ar.consumesAction() && !result.consumesAction()) {
                result = ar;
            } else if(ar.shouldSwing() && !result.shouldSwing()) {
                result = ar;
            }
        }
        if(result.consumesAction()) {
            startPrediction(player.clientLevel, id -> new ServerboundUseItemOnPacket(hand, hitResult, id));
            cir.setReturnValue(result);
        }
    }
}

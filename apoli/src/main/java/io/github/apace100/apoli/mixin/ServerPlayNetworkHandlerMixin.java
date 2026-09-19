package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.EndRespawningEntity;
import io.github.apace100.apoli.power.ActionOnItemUsePower;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleClientCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;", ordinal = 0))
    private void saveEndRespawnStatus(ServerboundClientCommandPacket packet, CallbackInfo ci) {
        ((EndRespawningEntity)this.player).setEndRespawning(true);
    }

    @Inject(method = "handleClientCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/critereon/ChangeDimensionTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/resources/ResourceKey;)V"))
    private void undoEndRespawnStatus(ServerboundClientCommandPacket packet, CallbackInfo ci) {
        ((EndRespawningEntity)this.player).setEndRespawning(false);
    }

    @Inject(method = "handleSetCarriedItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ServerboundSetCarriedItemPacket;getSlot()I", ordinal = 0))
    private void callActionOnUseStopBySwitching(ServerboundSetCarriedItemPacket packet, CallbackInfo ci) {
        if(player.isUsingItem()) {
            ActionOnItemUsePower.executeActions(player, player.getUseItem(), player.getUseItem(), ActionOnItemUsePower.TriggerType.STOP, ActionOnItemUsePower.PriorityPhase.ALL);
        }
    }

    @Inject(method = "handlePlayerAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;stopUsingItem()V"))
    private void callActionOnUseStopBySwappingHands(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        if(player.isUsingItem()) {
            ActionOnItemUsePower.executeActions(player, player.getUseItem(), player.getUseItem(), ActionOnItemUsePower.TriggerType.STOP, ActionOnItemUsePower.PriorityPhase.ALL);
        }
    }
}

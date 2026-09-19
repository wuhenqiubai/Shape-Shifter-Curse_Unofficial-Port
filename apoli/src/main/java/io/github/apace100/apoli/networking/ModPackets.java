package io.github.apace100.apoli.networking;

import io.github.apace100.apoli.Apoli;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class ModPackets {

    public static final ResourceLocation HANDSHAKE = Apoli.identifier("handshake");

    public static final CustomPacketPayload.Type<UseActivePowersPacket> USE_ACTIVE_POWERS = new CustomPacketPayload.Type<>(Apoli.identifier("use_active_powers")); // C -> S
    public static final CustomPacketPayload.Type<PowerListPacket> POWER_LIST = new CustomPacketPayload.Type<>(Apoli.identifier("power_list")); // S -> C
    public static final CustomPacketPayload.Type<SyncPowerPacket> SYNC_POWER = new CustomPacketPayload.Type<>(Apoli.identifier("sync_power")); // S -> C

    public static final CustomPacketPayload.Type<PlayerLandedPacket> PLAYER_LANDED = new CustomPacketPayload.Type<>(Apoli.identifier("player_landed")); // C -> S

    public static final CustomPacketPayload.Type<PlayerMountPacket> PLAYER_MOUNT = new CustomPacketPayload.Type<>(Apoli.identifier("player_mount")); // S -> C
    public static final CustomPacketPayload.Type<PlayerDismountPacket> PLAYER_DISMOUNT = new CustomPacketPayload.Type<>(Apoli.identifier("player_dismount")); // S -> C

    public static final CustomPacketPayload.Type<PreventedEntityUsePacket> PREVENTED_ENTITY_USE = new CustomPacketPayload.Type<>(Apoli.identifier("prevented_entity_use")); // C -> S

    public static final CustomPacketPayload.Type<SetAttackerPacket> SET_ATTACKER = new CustomPacketPayload.Type<>(Apoli.identifier("set_attacker")); // S -> C

    public static final CustomPacketPayload.Type<SyncStatusEffectPacket> SYNC_STATUS_EFFECT = new CustomPacketPayload.Type<>(Apoli.identifier("sync_status_effect")); // S -> C

    public static void init() {
        PayloadTypeRegistry.playC2S().register(USE_ACTIVE_POWERS, UseActivePowersPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(POWER_LIST, PowerListPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SYNC_POWER, SyncPowerPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(PLAYER_LANDED, PlayerLandedPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(PLAYER_MOUNT, PlayerMountPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(PLAYER_DISMOUNT, PlayerDismountPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(PREVENTED_ENTITY_USE, PreventedEntityUsePacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SET_ATTACKER, SetAttackerPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SYNC_STATUS_EFFECT, SyncStatusEffectPacket.CODEC);
    }
}

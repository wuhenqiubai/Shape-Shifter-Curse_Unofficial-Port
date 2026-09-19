package io.github.apace100.calio.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class CalioNetworking {

    public static final CustomPacketPayload.Type<SyncDataObjectRegistryPacket<?>> SYNC_DATA_OBJECT_REGISTRY = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("calio", "sync_data_object_registry"));

    public static void init() {
        PayloadTypeRegistry.playS2C().register(SYNC_DATA_OBJECT_REGISTRY, SyncDataObjectRegistryPacket.CODEC);
    }
}

package io.github.apace100.calio.network;

import io.github.apace100.calio.registry.DataObject;
import io.github.apace100.calio.registry.DataObjectRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SyncDataObjectRegistryPacket<T extends DataObject<T>> (DataObjectRegistry<T> registry) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncDataObjectRegistryPacket<?>> CODEC = StreamCodec.composite(
        DataObjectRegistry.STREAM_CODEC, SyncDataObjectRegistryPacket::registry,
        SyncDataObjectRegistryPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CalioNetworking.SYNC_DATA_OBJECT_REGISTRY;
    }
}

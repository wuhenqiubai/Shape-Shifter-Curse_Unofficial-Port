package io.github.apace100.apoli.networking;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SyncPowerPacket(
    int entityId,
    Identifier powerId,
    CompoundTag powerNbtContainer
) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, SyncPowerPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, SyncPowerPacket::entityId,
        Identifier.STREAM_CODEC, SyncPowerPacket::powerId,
        ByteBufCodecs.COMPOUND_TAG, SyncPowerPacket::powerNbtContainer,
        SyncPowerPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ModPackets.SYNC_POWER;
    }
}

package io.github.apace100.apoli.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PreventedEntityUsePacket(
    int otherEntityId,
    int handOrdinal
) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, PreventedEntityUsePacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, PreventedEntityUsePacket::otherEntityId,
        ByteBufCodecs.VAR_INT, PreventedEntityUsePacket::handOrdinal,
        PreventedEntityUsePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ModPackets.PREVENTED_ENTITY_USE;
    }
}

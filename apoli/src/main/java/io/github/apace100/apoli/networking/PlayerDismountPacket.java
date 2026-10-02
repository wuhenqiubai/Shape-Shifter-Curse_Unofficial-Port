package io.github.apace100.apoli.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PlayerDismountPacket(
    int ridingEntity
) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, PlayerDismountPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, PlayerDismountPacket::ridingEntity,
        PlayerDismountPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ModPackets.PLAYER_DISMOUNT;
    }
}

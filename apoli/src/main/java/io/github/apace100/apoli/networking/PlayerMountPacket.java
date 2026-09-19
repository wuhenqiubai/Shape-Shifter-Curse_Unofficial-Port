package io.github.apace100.apoli.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PlayerMountPacket(
    int ridingEntity,
    int vehicleEntity
) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, PlayerMountPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, PlayerMountPacket::ridingEntity,
        ByteBufCodecs.VAR_INT, PlayerMountPacket::vehicleEntity,
        PlayerMountPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ModPackets.PLAYER_MOUNT;
    }
}

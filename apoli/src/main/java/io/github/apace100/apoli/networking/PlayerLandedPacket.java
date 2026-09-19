package io.github.apace100.apoli.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class PlayerLandedPacket implements CustomPacketPayload {
    public static final PlayerLandedPacket INSTANCE = new PlayerLandedPacket();
    public static final StreamCodec<ByteBuf, PlayerLandedPacket> CODEC = StreamCodec.unit(INSTANCE);

    private PlayerLandedPacket() {}

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ModPackets.PLAYER_LANDED;
    }
}

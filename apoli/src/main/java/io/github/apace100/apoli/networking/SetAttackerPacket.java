package io.github.apace100.apoli.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Optional;

public record SetAttackerPacket(
    int entityId,
    Optional<Integer> attackingEntityId
) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, SetAttackerPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, SetAttackerPacket::entityId,
        ByteBufCodecs.optional(ByteBufCodecs.VAR_INT), SetAttackerPacket::attackingEntityId,
        SetAttackerPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ModPackets.SET_ATTACKER;
    }
}

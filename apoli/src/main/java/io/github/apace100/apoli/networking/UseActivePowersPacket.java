package io.github.apace100.apoli.networking;

import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

public record UseActivePowersPacket(
    List<PowerType<?>> powers
) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, UseActivePowersPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.<FriendlyByteBuf, PowerType<?>>list().apply(StreamCodec.of((buf, power) -> buf.writeIdentifier(power.getIdentifier()), (buf) -> PowerTypeRegistry.get(buf.readIdentifier()))), UseActivePowersPacket::powers,
        UseActivePowersPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ModPackets.USE_ACTIVE_POWERS;
    }
}

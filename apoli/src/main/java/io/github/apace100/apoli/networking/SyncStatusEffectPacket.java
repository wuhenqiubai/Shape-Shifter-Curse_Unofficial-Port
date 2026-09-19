package io.github.apace100.apoli.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Optional;

public record SyncStatusEffectPacket(
    int entityId,
    byte updateType,
    Optional<MobEffectInstance> effectInstance
) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncStatusEffectPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, SyncStatusEffectPacket::entityId,
        ByteBufCodecs.BYTE, SyncStatusEffectPacket::updateType,
        ByteBufCodecs.optional(MobEffectInstance.STREAM_CODEC), SyncStatusEffectPacket::effectInstance,
        SyncStatusEffectPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ModPackets.SYNC_STATUS_EFFECT;
    }
}

package net.onixary.shapeShifterCurseFabric.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic byte-buffer payload for migration from old Identifier+PacketByteBuf API
 * to Fabric 1.21 CustomPayload API.
 * Each packet ID still uses an Identifier; the payload wraps the raw buf.
 */
public record BytePayload(Type<BytePayload> id, FriendlyByteBuf data) implements CustomPacketPayload {

    private static final ConcurrentHashMap<Identifier, Type<BytePayload>> IDS = new ConcurrentHashMap<>();
    private static final java.util.HashSet<Identifier> REGISTERED_S2C = new java.util.HashSet<>();
    private static final java.util.HashSet<Identifier> REGISTERED_C2S = new java.util.HashSet<>();

    public static Type<BytePayload> id(Identifier identifier) {
	    return IDS.computeIfAbsent(identifier, Type::new);
    }

    /** Create a per-ID CODEC whose decoder returns the correct Id */
    private static StreamCodec<FriendlyByteBuf, BytePayload> codecFor(Type<BytePayload> pid) {
        return StreamCodec.ofMember(
            (payload, buf) -> buf.writeBytes(payload.data.readBytes(payload.data.readableBytes())),
            buf -> new BytePayload(pid, new FriendlyByteBuf(buf.readBytes(buf.readableBytes())))
        );
    }

    /** Shorthand: register S2C (idempotent) */
    public static void registerS2C(Identifier identifier) {
        if (REGISTERED_S2C.add(identifier)) {
            PayloadTypeRegistry.playS2C().register(id(identifier), codecFor(id(identifier)));
        }
    }

    /** Shorthand: register C2S (idempotent) */
    public static void registerC2S(Identifier identifier) {
        if (REGISTERED_C2S.add(identifier)) {
            PayloadTypeRegistry.playC2S().register(id(identifier), codecFor(id(identifier)));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return id; }
}
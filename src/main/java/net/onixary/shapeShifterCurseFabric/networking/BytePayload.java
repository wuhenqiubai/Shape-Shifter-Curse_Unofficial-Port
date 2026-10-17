package net.onixary.shapeShifterCurseFabric.networking;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import java.util.concurrent.ConcurrentHashMap;

public record BytePayload(Type<BytePayload> id, FriendlyByteBuf data) implements CustomPacketPayload {

    private static final ConcurrentHashMap<Identifier, Type<BytePayload>> IDS = new ConcurrentHashMap<>();
    private static final java.util.HashSet<Identifier> REGISTERED_S2C = new java.util.HashSet<>();
    private static final java.util.HashSet<Identifier> REGISTERED_C2S = new java.util.HashSet<>();

    public static Type<BytePayload> id(Identifier identifier) {
	    return IDS.computeIfAbsent(identifier, Type::new);
    }

    private static final int MAX_SIZE = 1048576;

    private static StreamCodec<FriendlyByteBuf, BytePayload> codecFor(Type<BytePayload> pid) {
        return StreamCodec.ofMember(
            (payload, buf) -> {
                FriendlyByteBuf src = payload.data;
                buf.writeBytes(src, src.readerIndex(), src.readableBytes());
            },
            buf -> {
                int length = buf.readableBytes();
                if (length > MAX_SIZE) {
                    throw new IllegalArgumentException(
                            "Payload " + pid.id() + " may not be larger than " + MAX_SIZE + " bytes, got " + length);
                }
                byte[] bytes = new byte[length];
                buf.readBytes(bytes);
                return new BytePayload(pid, new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes)));
            }
        );
    }

    public static void registerS2C(Identifier identifier) {
        if (REGISTERED_S2C.add(identifier)) {
            PayloadTypeRegistry.playS2C().register(id(identifier), codecFor(id(identifier)));
        }
    }

    public static void registerC2S(Identifier identifier) {
        if (REGISTERED_C2S.add(identifier)) {
            PayloadTypeRegistry.playC2S().register(id(identifier), codecFor(id(identifier)));
        }
    }

    public static java.util.Set<Identifier> registeredC2S() {
        return java.util.Set.copyOf(REGISTERED_C2S);
    }

    public static java.util.Set<Identifier> registeredS2C() {
        return java.util.Set.copyOf(REGISTERED_S2C);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return id; }
}
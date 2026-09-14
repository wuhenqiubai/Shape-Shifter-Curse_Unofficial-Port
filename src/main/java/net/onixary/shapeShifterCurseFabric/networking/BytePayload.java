package net.onixary.shapeShifterCurseFabric.networking;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
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

    /** 单包字节上限，与原版 DiscardedPayload 对齐（防对端声明超大长度导致的内存分配）。 */
    private static final int MAX_SIZE = 1048576;

    /** Create a per-ID CODEC whose decoder returns the correct Id */
    private static StreamCodec<FriendlyByteBuf, BytePayload> codecFor(Type<BytePayload> pid) {
        return StreamCodec.ofMember(
            // 用三参 writeBytes（不推进源 buffer 的 readerIndex），保证同一 payload 实例可被重复编码。
            // 若改回 readBytes(...)，编码会消耗 payload.data，第二次编码将静默发出空包。
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
                // Unpooled 包装：字节交由 GC 管理，无需 release。
                // 不要改回 new FriendlyByteBuf(buf.readBytes(...)) —— 那会产生一个池化 buffer，生命周期归属不明。
                return new BytePayload(pid, new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes)));
            }
        );
    }

    /** Shorthand: register S2C (idempotent) */
    public static void registerS2C(Identifier identifier) {
        if (REGISTERED_S2C.add(identifier)) {
            PayloadTypeRegistry.clientboundPlay().register(id(identifier), codecFor(id(identifier)));
        }
    }

    /** Shorthand: register C2S (idempotent) */
    public static void registerC2S(Identifier identifier) {
        if (REGISTERED_C2S.add(identifier)) {
            PayloadTypeRegistry.serverboundPlay().register(id(identifier), codecFor(id(identifier)));
        }
    }

    /** 供自检读取：本端已注册的 C2S 包 id（防御性副本，见 NetworkRegistrationSelfCheck）。 */
    public static java.util.Set<Identifier> registeredC2S() {
        return java.util.Set.copyOf(REGISTERED_C2S);
    }

    /** 供自检读取：本端已注册的 S2C 包 id（防御性副本，见 NetworkRegistrationSelfCheck）。 */
    public static java.util.Set<Identifier> registeredS2C() {
        return java.util.Set.copyOf(REGISTERED_S2C);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return id; }
}
package net.onixary.shapeShifterCurseFabric.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import java.util.Set;
import java.util.TreeSet;

/**
 * 网络包注册自检（开发期防呆）。
 *
 * <p><b>防的是哪一半</b>：Fabric 的两步注册里，「挂了 receiver 但没注册 payload type」那一半由
 * {@code GlobalReceiverRegistry.assertPayloadType} 兜底 —— 启动即抛
 * {@code Cannot register handler as no payload type has been registered}，<b>不会静默</b>。
 * 真正裸奔的是<b>反方向</b>：{@code BytePayload.registerC2S(id)} 调了、却忘了挂 receiver ——
 * 客户端照样发得出去，服务端没人处理，<b>不崩不报错、静默失效</b>。本类专门盯这一半。</p>
 *
 * <p><b>为什么零误报</b>：判据是「已注册的 id」减去「已挂 receiver 的 id」。能进前者的前提是
 * 有人主动调过 {@code registerC2S}/{@code registerS2C}，所以差集里每一条都必然是漏挂 ——
 * 从未注册也从未挂载的死常量压根不进集合，不参与判断。</p>
 *
 * <p><b>为什么顺带覆盖 SSCAddon</b>：Addon 复用本模组的 {@link BytePayload}（Modrinth 运行时
 * mod 依赖，非 include、非 shade），Fabric 默认平铺类加载下是同一个 Class、同一份静态账本，
 * 所以它注册的包也在比对范围内，无需在 Addon 侧重复实现。</p>
 *
 * <p><b>为什么用启动完成事件而非 initializer 末尾</b>：注册分散在 4 个 initializer
 * （本模组 + Origins 集成，各自又分 common/client），且 {@code fabric.mod.json} 的 entrypoint
 * 顺序决定 Origins 在本模组之后 —— 插在任一 initializer 末尾都会漏掉后面的。挂在启动完成
 * 事件上，可确保所有 initializer 都已执行完毕。</p>
 *
 * <p><b>client 相关代码为何不写在本类里</b>：本类会在专用服务端被加载并执行（{@code run()} 由
 * {@code SERVER_STARTED} 触发），而 {@code ClientPlayNetworking} 属 Fabric API client 源集、
 * 专用服务端上类缺失。故 client 侧全部隔离到 {@link NetworkRegistrationSelfCheckClient}，
 * 只在本类的 {@code EnvType.CLIENT} 分支下触达 —— 结构上保证专用服务端永不加载该类。</p>
 */
public final class NetworkRegistrationSelfCheck {

    private NetworkRegistrationSelfCheck() {
    }

    /** 在 common 入口（{@code ShapeShifterCurseFabric#onInitialize}）调用：服务端启动完成时校验。 */
    public static void registerServer() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> run());
    }

    /**
     * 在客户端入口（{@code ShapeShifterCurseFabricClient#onInitializeClient}）调用。
     * 方法体只引用同包的 client 隔离类，不含任何 client-only 类型，故在 common 侧声明是安全的。
     */
    public static void registerClient() {
        NetworkRegistrationSelfCheckClient.register();
    }

    private static void run() {
        // C2S：两端都能查。ServerNetworkingImpl 只依赖 common 侧类，物理客户端加载是安全的。
        report("C2S", BytePayload.registeredC2S(), ServerPlayNetworking.getGlobalReceivers(),
                "客户端发送后将无人处理（静默失效）");

        // S2C 只能在物理客户端查，且必须走隔离类（见类注释最后一段）
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            NetworkRegistrationSelfCheckClient.run();
        }
    }

    /** 比对「已注册的 id」与「已挂 receiver 的 id」，把差集逐条报出来。供 client 隔离类复用。 */
    static void report(String direction, Set<Identifier> registered,
                       Set<Identifier> withReceivers, String consequence) {
        Set<Identifier> orphans = new TreeSet<>(registered);
        orphans.removeAll(withReceivers);
        if (orphans.isEmpty()) {
            return;
        }
        ShapeShifterCurseFabric.LOGGER.error(
                "[网络自检] {} 方向有 {} 个包只注册了类型、没挂 receiver —— {}：",
                direction, orphans.size(), consequence);
        for (Identifier id : orphans) {
            ShapeShifterCurseFabric.LOGGER.error("[网络自检]   - {}", id);
        }
    }
}

package net.onixary.shapeShifterCurseFabric.networking;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * {@link NetworkRegistrationSelfCheck} 的客户端专属部分（class-loading isolation）。
 *
 * <p>本类引用的 {@link ClientLifecycleEvents} 与 {@link ClientPlayNetworking} 都属 Fabric API 的
 * client 源集，其内部引用 {@code net.minecraft.client.*} —— 专用服务端上这些类不存在。</p>
 *
 * <p>因此这里不写任何"靠环境判断兜底"的代码：把 client 引用全部收进本类，且只由
 * {@link NetworkRegistrationSelfCheck} 在 {@code EnvType.CLIENT} 分支下触达，使专用服务端
 * <b>结构上不可能加载本类</b>，而不是依赖 JVM 惰性解析的细节。</p>
 */
final class NetworkRegistrationSelfCheckClient {

    private NetworkRegistrationSelfCheckClient() {
    }

    /** 由 common 侧 {@code NetworkRegistrationSelfCheck#registerClient()} 调用。 */
    static void register() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> run());
    }

    /** S2C 方向的比对：客户端有没有为「已注册的 S2C 包」挂上 receiver。 */
    static void run() {
        NetworkRegistrationSelfCheck.report("S2C", BytePayload.registeredS2C(),
                ClientPlayNetworking.getGlobalReceivers(),
                "服务端发送后会被当作未知包丢弃");
    }
}

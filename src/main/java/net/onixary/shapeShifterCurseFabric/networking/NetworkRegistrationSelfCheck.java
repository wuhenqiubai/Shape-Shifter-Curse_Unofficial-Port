package net.onixary.shapeShifterCurseFabric.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import java.util.Set;
import java.util.TreeSet;

public final class NetworkRegistrationSelfCheck {

    private NetworkRegistrationSelfCheck() {
    }

    public static void registerServer() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> run());
    }

    public static void registerClient() {
        NetworkRegistrationSelfCheckClient.register();
    }

    private static void run() {
        report("C2S", BytePayload.registeredC2S(), ServerPlayNetworking.getGlobalReceivers(),
                "客户端发送后将无人处理（静默失效）");

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            NetworkRegistrationSelfCheckClient.run();
        }
    }

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

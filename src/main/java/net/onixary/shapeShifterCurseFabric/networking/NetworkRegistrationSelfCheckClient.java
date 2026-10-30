package net.onixary.shapeShifterCurseFabric.networking;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

final class NetworkRegistrationSelfCheckClient {

    private NetworkRegistrationSelfCheckClient() {
    }

    static void register() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> run());
    }

    static void run() {
        NetworkRegistrationSelfCheck.report("S2C", BytePayload.registeredS2C(),
                ClientPlayNetworking.getGlobalReceivers(),
                "服务端发送后会被当作未知包丢弃");
    }
}

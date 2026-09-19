package io.github.apace100.calio.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public class CalioNetworkingClient {

    public static void registerReceivers() {
        ClientPlayConnectionEvents.INIT.register(((clientPlayNetworkHandler, minecraftClient) -> {
            ClientPlayNetworking.registerReceiver(
                CalioNetworking.SYNC_DATA_OBJECT_REGISTRY,
                CalioNetworkingClient::onDataObjectRegistrySync
            );
        }));
    }

    private static void onDataObjectRegistrySync(
        SyncDataObjectRegistryPacket<?> packet,
        ClientPlayNetworking.Context context) {
        //DataObjectRegistry.getRegistry(packet.registryId()).receive(packet.entries(),
            //context.client().hasSingleplayerServer() ? r -> {} : context.client()::execute);
        /*minecraftClient.execute(() -> {
            DataObjectRegistry.getRegistry(registryId).receive(packetByteBuf);
        });*/
    }
}

package net.onixary.shapeShifterCurseFabric.integration.origins.networking;

import io.github.apace100.calio.data.SerializableData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;
import net.onixary.shapeShifterCurseFabric.integration.origins.OriginsClient;
import net.onixary.shapeShifterCurseFabric.integration.origins.badge.Badge;
import net.onixary.shapeShifterCurseFabric.integration.origins.badge.BadgeManager;
import net.onixary.shapeShifterCurseFabric.integration.origins.component.OriginComponent;
import net.onixary.shapeShifterCurseFabric.integration.origins.integration.OriginDataLoadedCallback;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.Origin;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.OriginLayer;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.OriginLayers;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.OriginRegistry;
import net.onixary.shapeShifterCurseFabric.integration.origins.registry.ModComponents;
import net.onixary.shapeShifterCurseFabric.integration.origins.screen.WaitForNextLayerScreen;
import net.onixary.shapeShifterCurseFabric.networking.BytePayload;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ModPacketsS2C {

    @Environment(EnvType.CLIENT)
    public static void register() {
        BytePayload.registerS2C(ModPackets.ORIGIN_LIST);
        BytePayload.registerS2C(ModPackets.LAYER_LIST);
        BytePayload.registerS2C(ModPackets.OPEN_ORIGIN_SCREEN);
        BytePayload.registerS2C(ModPackets.CONFIRM_ORIGIN);
        BytePayload.registerS2C(ModPackets.BADGE_LIST);

        ClientLoginNetworking.registerGlobalReceiver(ModPackets.HANDSHAKE, (client, handler, buf, responseConsumer) -> handleHandshake(client, handler, buf));
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.OPEN_ORIGIN_SCREEN), ModPacketsS2C::receiveOpenOriginScreen);
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.ORIGIN_LIST), ModPacketsS2C::receiveOriginList);
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.LAYER_LIST), ModPacketsS2C::receiveLayerList);
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.CONFIRM_ORIGIN), ModPacketsS2C::receiveOriginConfirmation);
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.BADGE_LIST), ModPacketsS2C::receiveBadgeList);
    }

	@Environment(EnvType.CLIENT)
	private static void receiveOriginConfirmation(BytePayload payload, ClientPlayNetworking.Context ctx) {
		FriendlyByteBuf buf = payload.data();
		OriginLayer layer = OriginLayers.getLayer(buf.readIdentifier());
		Origin origin = OriginRegistry.get(buf.readIdentifier());

		if (layer == null) {
			Origins.LOGGER.warn("Received origin confirmation with null layer");
			return;
		}

		if (origin == null) {
			Origins.LOGGER.warn("Received origin confirmation with null origin");
			return;
		}

		ctx.client().execute(() -> {
			if (ctx.client().player == null) {
				Origins.LOGGER.warn("Client player is null when receiving origin confirmation");
				return;
			}

			OriginComponent component = ModComponents.ORIGIN.get(ctx.client().player);
			if (component == null) {
				Origins.LOGGER.warn("OriginComponent is null for player when receiving origin confirmation");
				return;
			}

			component.setOrigin(layer, origin);
			if (ctx.client().screen instanceof WaitForNextLayerScreen) {
				((WaitForNextLayerScreen) ctx.client().screen).openSelection();
			}
		});
	}

	@Environment(EnvType.CLIENT)
	private static CompletableFuture<FriendlyByteBuf> handleHandshake(Minecraft minecraftClient, ClientHandshakePacketListenerImpl clientLoginNetworkHandler, FriendlyByteBuf packetByteBuf) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeInt(Origins.SEMVER.length);
		for (int i = 0; i < Origins.SEMVER.length; i++) {
			buf.writeInt(Origins.SEMVER[i]);
		}
		OriginsClient.isServerRunningOrigins = true;
		return CompletableFuture.completedFuture(buf);
	}

	@Environment(EnvType.CLIENT)
	private static void receiveOpenOriginScreen(BytePayload payload, ClientPlayNetworking.Context ctx) {
		// 用于显示Origins选择页面，不需要，将其注释掉：
        /*PacketByteBuf buf = payload.data();
        boolean showDirtBackground = buf.readBoolean();
        ctx.client().execute(() -> {
            ArrayList<OriginLayer> layers = new ArrayList<>();
            OriginComponent component = ModComponents.ORIGIN.get(ctx.client().player);
            OriginLayers.getLayers().forEach(layer -> {
                if(layer.isEnabled() && !component.hasOrigin(layer)) {
                    layers.add(layer);
                }
            });
            Collections.sort(layers);
            ctx.client().setScreen(new ChooseOriginScreen(layers, 0, showDirtBackground));
        });*/
	}

	@Environment(EnvType.CLIENT)
	private static void receiveOriginList(BytePayload payload, ClientPlayNetworking.Context ctx) {
		FriendlyByteBuf packetByteBuf = payload.data();
		try {
			Minecraft minecraftClient = ctx.client();
			Identifier[] ids = new Identifier[packetByteBuf.readInt()];
			SerializableData.Instance[] origins = new SerializableData.Instance[ids.length];
			RegistryFriendlyByteBuf originRegBuf = null;
			if (minecraftClient.level != null) {
				originRegBuf = new RegistryFriendlyByteBuf(packetByteBuf, minecraftClient.level.registryAccess());
			}
			for (int i = 0; i < origins.length; i++) {
				if (originRegBuf != null) {
					ids[i] = Identifier.tryParse(originRegBuf.readUtf());
				}
				origins[i] = Origin.DATA.read(originRegBuf);
			}
			minecraftClient.execute(() -> {
				OriginsClient.isServerRunningOrigins = true;
				OriginRegistry.reset();
				for (int i = 0; i < ids.length; i++) {
					OriginRegistry.register(ids[i], Origin.createFromData(ids[i], origins[i]));
				}
			});
		} catch (Exception e) {
			Origins.LOGGER.error(e);
		}
	}

    @Environment(EnvType.CLIENT)
    private static void receiveLayerList(BytePayload payload, ClientPlayNetworking.Context ctx) {
        FriendlyByteBuf packetByteBuf = payload.data();
        try {
            Minecraft minecraftClient = ctx.client();
            int layerCount = packetByteBuf.readInt();
            RegistryFriendlyByteBuf layerRegBuf = null;
            if (minecraftClient.level != null) {
                layerRegBuf = new RegistryFriendlyByteBuf(packetByteBuf, minecraftClient.level.registryAccess());
            }
            OriginLayer[] layers = new OriginLayer[layerCount];
            for (int i = 0; i < layerCount; i++) {
                if (layerRegBuf != null) {
                    layers[i] = OriginLayer.read(layerRegBuf);
                }
            }
            minecraftClient.execute(() -> {
                OriginLayers.clear();
                for (int i = 0; i < layerCount; i++) {
                    OriginLayers.add(layers[i]);
                }
                OriginDataLoadedCallback.EVENT.invoker().onDataLoaded(true);
            });
        } catch (Exception e) {
            Origins.LOGGER.error(e);
        }
    }

    @Environment(EnvType.CLIENT)
    private static void receiveBadgeList(BytePayload payload, ClientPlayNetworking.Context ctx) {
        FriendlyByteBuf packetByteBuf = payload.data();
        try {
            Minecraft minecraftClient = ctx.client();
            HashMap<Identifier, List<Badge>> badges = new HashMap<>();
            int count = packetByteBuf.readInt();
            for (int i = 0; i < count; i++) {
                Identifier powerId = packetByteBuf.readIdentifier();
                List<Badge> badgeList = new LinkedList<>();
                int badgeCount = packetByteBuf.readInt();
                RegistryFriendlyByteBuf badgeRegBuf = new RegistryFriendlyByteBuf(packetByteBuf, minecraftClient.level.registryAccess());
                for (int j = 0; j < badgeCount; j++) {
                    Badge badge = BadgeManager.REGISTRY.receiveDataObject(badgeRegBuf);
                    badgeList.add(badge);
                }
                badges.put(powerId, badgeList);
            }
            minecraftClient.execute(() -> {
                BadgeManager.clear();
                for (Map.Entry<Identifier, List<Badge>> badgeEntry : badges.entrySet()) {
                    for (Badge badge : badgeEntry.getValue()) {
                        BadgeManager.putPowerBadge(badgeEntry.getKey(), badge);
                    }
                }
            });
        } catch (Exception e) {
            Origins.LOGGER.error(e);
        }
    }
}
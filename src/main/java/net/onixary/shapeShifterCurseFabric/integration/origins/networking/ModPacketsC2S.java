package net.onixary.shapeShifterCurseFabric.integration.origins.networking;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;
import net.onixary.shapeShifterCurseFabric.integration.origins.component.OriginComponent;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.Origin;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.OriginLayer;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.OriginLayers;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.OriginRegistry;
import net.onixary.shapeShifterCurseFabric.integration.origins.registry.ModComponents;
import net.onixary.shapeShifterCurseFabric.networking.BytePayload;

import java.util.List;
import java.util.Random;

public class ModPacketsC2S {

    public static void register() {
        registerClient();

        ServerPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.CHOOSE_ORIGIN), ModPacketsC2S::chooseOrigin);
        ServerPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.CHOOSE_RANDOM_ORIGIN), ModPacketsC2S::chooseRandomOrigin);
    }

    public static void registerClient() {
        BytePayload.registerC2S(ModPackets.CHOOSE_ORIGIN);
        BytePayload.registerC2S(ModPackets.CHOOSE_RANDOM_ORIGIN);
    }

    private static void chooseOrigin(BytePayload payload, ServerPlayNetworking.Context ctx) {
        ServerPlayerEntity player = ctx.player();
        PacketByteBuf buf = payload.data();
        String originId = buf.readString(32767);
        String layerId = buf.readString(32767);

        if (player.getServer() == null) {
            Origins.LOGGER.warn("Player server is null");
            return;
        }

        player.getServer().execute(() -> {
            OriginComponent component = ModComponents.ORIGIN.get(player);
            if (component == null) {
                Origins.LOGGER.warn("OriginComponent is null for player: {}", player.getName().getString());
                return;
            }

            Identifier layerIdentifier = Identifier.tryParse(layerId);
            if (layerIdentifier == null) {
                Origins.LOGGER.warn("Invalid layer ID: {}", layerId);
                return;
            }

            OriginLayer layer = OriginLayers.getLayer(layerIdentifier);
            if (layer == null) {
                Origins.LOGGER.warn("Layer not found: {}", layerIdentifier);
                return;
            }

            if (!component.hasAllOrigins() && !component.hasOrigin(layer)) {
                Identifier id = Identifier.tryParse(originId);
                if (id == null) {
                    Origins.LOGGER.warn("Invalid origin ID: {}", originId);
                    return;
                }

                Origin origin = OriginRegistry.get(id);
                if (origin == null) {
                    Origins.LOGGER.warn("Origin not found: {}", id);
                    return;
                }

                if (origin.isChoosable() && layer.contains(origin, player)) {
                    boolean hadOriginBefore = component.hadOriginBefore();
                    boolean hadAllOrigins = component.hasAllOrigins();
                    component.setOrigin(layer, origin);
                    component.checkAutoChoosingLayers(player, false);
                    component.sync();
                    if (component.hasAllOrigins() && !hadAllOrigins) {
                        OriginComponent.onChosen(player, hadOriginBefore);
                    }
                    Origins.LOGGER.info("Player {} chose Origin: {}, for layer: {}", player.getName().getString(), originId, layerId);
                } else {
                    Origins.LOGGER.info("Player {} tried to choose unchoosable Origin for layer {}: {}.", player.getName().getString(), layerId, originId);
                    component.setOrigin(layer, Origin.EMPTY);
                }
                confirmOrigin(player, layer, component.getOrigin(layer));
                component.sync();
            } else {
                Origins.LOGGER.warn("Player {} tried to choose origin for layer {} while having one already.", player.getName().getString(), layerId);
            }
        });
    }

    private static void chooseRandomOrigin(BytePayload payload, ServerPlayNetworking.Context ctx) {
        ServerPlayerEntity player = ctx.player();
        PacketByteBuf buf = payload.data();
        String layerId = buf.readString(32767);

        if (player.getServer() == null) {
            Origins.LOGGER.warn("Player server is null");
            return;
        }

        player.getServer().execute(() -> {
            OriginComponent component = ModComponents.ORIGIN.get(player);
            if (component == null) {
                Origins.LOGGER.warn("OriginComponent is null for player: {}", player.getName().getString());
                return;
            }

            Identifier layerIdentifier = Identifier.tryParse(layerId);
            if (layerIdentifier == null) {
                Origins.LOGGER.warn("Invalid layer ID: {}", layerId);
                return;
            }

            OriginLayer layer = OriginLayers.getLayer(layerIdentifier);
            if (layer == null) {
                Origins.LOGGER.warn("Layer not found: {}", layerIdentifier);
                return;
            }

            if (!component.hasAllOrigins() && !component.hasOrigin(layer)) {
                List<Identifier> randomOrigins = layer.getRandomOrigins(player);
                if (layer.isRandomAllowed() && randomOrigins != null && !randomOrigins.isEmpty()) {
                    Identifier randomOrigin = randomOrigins.get(new Random().nextInt(randomOrigins.size()));
                    Origin origin = OriginRegistry.get(randomOrigin);
                    if (origin == null) {
                        Origins.LOGGER.warn("Random origin not found: {}", randomOrigin);
                        return;
                    }

                    boolean hadOriginBefore = component.hadOriginBefore();
                    boolean hadAllOrigins = component.hasAllOrigins();
                    component.setOrigin(layer, origin);
                    component.checkAutoChoosingLayers(player, false);
                    component.sync();
                    if (component.hasAllOrigins() && !hadAllOrigins) {
                        OriginComponent.onChosen(player, hadOriginBefore);
                    }
                    Origins.LOGGER.info("Player {} was randomly assigned the following Origin: {}, for layer: {}", player.getName().getString(), randomOrigin, layerId);
                } else {
                    Origins.LOGGER.info("Player {} tried to choose a random Origin for layer {}, which is not allowed!", player.getName().getString(), layerId);
                    component.setOrigin(layer, Origin.EMPTY);
                }
                confirmOrigin(player, layer, component.getOrigin(layer));
                component.sync();
            } else {
                Origins.LOGGER.warn("Player {} tried to choose origin for layer {} while having one already.", player.getName().getString(), layerId);
            }
        });
    }

    private static void confirmOrigin(ServerPlayerEntity player, OriginLayer layer, Origin origin) {
        if (layer == null || origin == null) {
            Origins.LOGGER.warn("Cannot confirm origin: layer or origin is null");
            return;
        }

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeIdentifier(layer.getIdentifier());
        buf.writeIdentifier(origin.getIdentifier());
        ServerPlayNetworking.send(player, new BytePayload(BytePayload.id(ModPackets.CONFIRM_ORIGIN), buf));
    }
}
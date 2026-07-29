package net.onixary.shapeShifterCurseFabric.integration.origins.mixin;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.onixary.shapeShifterCurseFabric.integration.origins.badge.BadgeManager;
import net.onixary.shapeShifterCurseFabric.integration.origins.component.OriginComponent;
import net.onixary.shapeShifterCurseFabric.integration.origins.networking.ModPackets;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.Origin;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.OriginLayers;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.OriginRegistry;
import net.onixary.shapeShifterCurseFabric.integration.origins.registry.ModComponents;
import net.onixary.shapeShifterCurseFabric.networking.BytePayload;
import org.ladysnake.cca.api.v3.component.ComponentProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@SuppressWarnings("rawtypes")
@Mixin(PlayerList.class)
public abstract class LoginMixin {

    @Shadow public abstract List<ServerPlayer> getPlayers();

    @Inject(at = @At("TAIL"), method = "placeNewPlayer")
    private void openOriginsGui(Connection connection, ServerPlayer player, CommonListenerCookie clientData, CallbackInfo ci) {
        OriginComponent component = ModComponents.ORIGIN.get(player);
        net.minecraft.core.RegistryAccess registryLookup = player.level().registryAccess();

        RegistryFriendlyByteBuf originListData = new RegistryFriendlyByteBuf(Unpooled.buffer(), registryLookup);
        originListData.writeInt(OriginRegistry.size() - 1);
        OriginRegistry.entries().forEach((entry) -> {
            if(entry.getValue() != Origin.EMPTY) {
                originListData.writeIdentifier(entry.getKey());
                entry.getValue().write(originListData);
            }
        });

        RegistryFriendlyByteBuf originLayerData = new RegistryFriendlyByteBuf(Unpooled.buffer(), registryLookup);
        originLayerData.writeInt(OriginLayers.size());
        OriginLayers.getLayers().forEach((layer) -> {
            layer.write(originLayerData);
            if(layer.isEnabled()) {
                if(!component.hasOrigin(layer)) {
                    component.setOrigin(layer, Origin.EMPTY);
                }
            }
        });

        sendToPlayer(player, ModPackets.ORIGIN_LIST, originListData);
        sendToPlayer(player, ModPackets.LAYER_LIST, originLayerData);

        BadgeManager.sync(player);

        List<ServerPlayer> playerList = getPlayers();
        playerList.forEach(spe -> ModComponents.ORIGIN.syncWith(spe, (ComponentProvider)player));
        OriginComponent.sync(player);
        if(!component.hasAllOrigins()) {
            if(component.checkAutoChoosingLayers(player, true)) {
                component.sync();
            }
            if(component.hasAllOrigins()) {
                OriginComponent.onChosen(player, false);
            } else {
				FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
                data.writeBoolean(true);
                sendToPlayer(player, ModPackets.OPEN_ORIGIN_SCREEN, data);
            }
        }
    }

	@Unique
    private static void sendToPlayer(ServerPlayer player, Identifier id, FriendlyByteBuf buf) {
        ServerPlayNetworking.send(player, new BytePayload(BytePayload.id(id),  buf));
    }
}
package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.onixary.shapeShifterCurseFabric.networking.ModPacketsS2CServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class LoginMixin {
    @Inject(at = @At("TAIL"), method = "placeNewPlayer")
    private void onPlayerConnect(Connection connection, ServerPlayer player, CommonListenerCookie clientData, CallbackInfo ci) {
        ModPacketsS2CServer.sendPlayerLogin(player);
    }
}

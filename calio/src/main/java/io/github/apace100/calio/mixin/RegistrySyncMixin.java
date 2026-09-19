package io.github.apace100.calio.mixin;

import io.github.apace100.calio.registry.DataObjectRegistry;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class RegistrySyncMixin {


    @Shadow @Final private MinecraftServer server;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;initInventoryMenu()V"), method = "placeNewPlayer")
    private void autoSyncDataObjectRegistries(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        DataObjectRegistry.performAutoSync(player);
    }
}

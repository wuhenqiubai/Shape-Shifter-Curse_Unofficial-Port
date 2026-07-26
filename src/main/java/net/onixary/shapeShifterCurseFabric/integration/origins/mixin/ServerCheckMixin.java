package net.onixary.shapeShifterCurseFabric.integration.origins.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.onixary.shapeShifterCurseFabric.integration.origins.OriginsClient;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ConnectScreen.class)
public class ServerCheckMixin {

    @Inject(method = "startConnecting", at = @At("HEAD"))
    private static void resetServerOriginsState(Screen screen, Minecraft client, ServerAddress address, ServerData info, boolean quickPlay, @Nullable TransferState cookieStorage, CallbackInfo ci) {
        OriginsClient.isServerRunningOrigins = false;
    }
}

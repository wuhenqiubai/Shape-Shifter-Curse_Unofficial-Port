package net.onixary.shapeShifterCurseFabric.integration.origins;

import io.github.apace100.apoli.integration.PowerClearCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.onixary.shapeShifterCurseFabric.integration.origins.networking.ModPacketsS2C;
import net.onixary.shapeShifterCurseFabric.integration.origins.registry.ModBlocks;
import net.onixary.shapeShifterCurseFabric.integration.origins.registry.ModEntities;
import net.onixary.shapeShifterCurseFabric.integration.origins.util.PowerKeyManager;

public class OriginsClient implements ClientModInitializer {

    // public static KeyBinding usePrimaryActivePowerKeybind;
    // public static KeyBinding useSecondaryActivePowerKeybind;
    // public static KeyBinding viewCurrentOriginKeybind;

    public static boolean isServerRunningOrigins = false;

    @Override
    @Environment(EnvType.CLIENT)
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.TEMPORARY_COBWEB, RenderType.cutout());

        EntityRendererRegistry.register(ModEntities.ENDERIAN_PEARL, ThrownItemRenderer::new);

        ModPacketsS2C.register();

        // usePrimaryActivePowerKeybind = new KeyBinding("key.origins.primary_active", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category." + ShapeShifterCurseFabric.MOD_ID);
        // useSecondaryActivePowerKeybind = new KeyBinding("key.origins.secondary_active", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category." + ShapeShifterCurseFabric.MOD_ID);
        // // viewCurrentOriginKeybind = new KeyBinding("key.origins.view_origin", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, "category." + Origins.MODID);
        //
        // ApoliClient.registerPowerKeybinding("key.origins.primary_active", usePrimaryActivePowerKeybind);
        // ApoliClient.registerPowerKeybinding("key.origins.secondary_active", useSecondaryActivePowerKeybind);
        // ApoliClient.registerPowerKeybinding("primary", usePrimaryActivePowerKeybind);
        // ApoliClient.registerPowerKeybinding("secondary", useSecondaryActivePowerKeybind);
        //
        // // "none" is the default key used when none is specified.
        // ApoliClient.registerPowerKeybinding("none", usePrimaryActivePowerKeybind);
        //
        // KeyBindingHelper.registerKeyBinding(usePrimaryActivePowerKeybind);
        // KeyBindingHelper.registerKeyBinding(useSecondaryActivePowerKeybind);
        // KeyBindingHelper.registerKeyBinding(viewCurrentOriginKeybind);

        // ClientTickEvents.START_CLIENT_TICK.register(tick -> {
        //     // 用于显示当前的起源信息，不需要，将其注释掉
        //     /*while(viewCurrentOriginKeybind.wasPressed()) {
        //         if(!(MinecraftClient.getInstance().currentScreen instanceof ViewOriginScreen)) {
        //             MinecraftClient.getInstance().setScreen(new ViewOriginScreen());
        //         }
        //     }*/
        // });

        PowerClearCallback.EVENT.register(PowerKeyManager::clearCache);
    }
}
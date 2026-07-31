package net.onixary.shapeShifterCurseFabric.integration.origins;

import io.github.apace100.apoli.integration.PowerClearCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.onixary.shapeShifterCurseFabric.integration.origins.networking.ModPacketsS2C;
import net.onixary.shapeShifterCurseFabric.integration.origins.registry.ModBlocks;
import net.onixary.shapeShifterCurseFabric.integration.origins.registry.ModEntities;
import net.onixary.shapeShifterCurseFabric.integration.origins.util.PowerKeyManager;

public class OriginsClient implements ClientModInitializer {

	public static boolean isServerRunningOrigins = false;

    @Override
    @Environment(EnvType.CLIENT)
    public void onInitializeClient() {
        BlockRenderLayerMap.putBlock(ModBlocks.TEMPORARY_COBWEB, ChunkSectionLayer.CUTOUT);

        EntityRendererRegistry.register(ModEntities.ENDERIAN_PEARL, ThrownItemRenderer::new);

        ModPacketsS2C.register();

        PowerClearCallback.EVENT.register(PowerKeyManager::clearCache);
    }
}
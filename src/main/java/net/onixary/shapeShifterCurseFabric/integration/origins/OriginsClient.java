package net.onixary.shapeShifterCurseFabric.integration.origins;

import io.github.apace100.apoli.integration.PowerClearCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.onixary.shapeShifterCurseFabric.integration.origins.networking.ModPacketsS2C;
import net.onixary.shapeShifterCurseFabric.integration.origins.registry.ModEntities;
import net.onixary.shapeShifterCurseFabric.integration.origins.util.PowerKeyManager;

public class OriginsClient implements ClientModInitializer {

	public static boolean isServerRunningOrigins = false;

    @Override
    @Environment(EnvType.CLIENT)
    public void onInitializeClient() {
        // 26.1 起无需手工声明方块渲染层：BakedQuad.MaterialInfo.of(...) 会用
        // ChunkSectionLayer.byTransparency(该 quad 所用 sprite 的透明度) 自动推导，Fabric 也据此删掉了
        // BlockRenderLayerMap。TEMPORARY_COBWEB 用的是原版 minecraft:block/cobweb（二值 alpha），
        // 自动推导结果就是 CUTOUT，与原手工声明一致。

        EntityRendererRegistry.register(ModEntities.ENDERIAN_PEARL, ThrownItemRenderer::new);

        ModPacketsS2C.register();

        PowerClearCallback.EVENT.register(PowerKeyManager::clearCache);
    }
}
package net.onixary.shapeShifterCurseFabric.entity;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class RegCustomEntityRenderer {
    static {
        EntityRendererRegistry.register(RegCustomEntity.WEB_BULLET, ThrownItemRenderer::new);
        EntityRendererRegistry.register(RegCustomEntity.POTION_CHARM_ARROW, ThrownItemRenderer::new);
    }

    public static void init() {
    }
}

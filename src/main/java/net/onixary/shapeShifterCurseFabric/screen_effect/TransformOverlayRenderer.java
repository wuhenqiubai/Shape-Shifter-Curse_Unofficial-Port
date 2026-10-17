package net.onixary.shapeShifterCurseFabric.screen_effect;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;

@Environment(EnvType.CLIENT)
public final class TransformOverlayRenderer {

    private static boolean hudRegistered = false;

    private TransformOverlayRenderer() {
    }

    public static void render() {
        ensureHudRegistered();
    }

    private static void ensureHudRegistered() {
        if (hudRegistered) {
            return;
        }
        hudRegistered = true;
        HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> renderHud(guiGraphics));
    }

    private static void renderHud(GuiGraphics guiGraphics) {
        TransformOverlay overlay = TransformOverlay.INSTANCE;
        if (!overlay.enableOverlay) {
            if (overlay.strength_black <= 0.01f && overlay.strength_nausea <= 0.01f) {
                return;
            }
            overlay.strength_black *= 0.85f;
            overlay.strength_nausea *= 0.85f;
        } else if (overlay.strength_black <= 0.01f && overlay.strength_nausea <= 0.01f) {
            return;
        }
        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();
        if (overlay.strength_nausea > 0.0f) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, overlay.nausea_texture, 0, 0, 0.0F, 0.0F, width, height, width, height, ARGB.white(overlay.strength_nausea));
        }
        if (overlay.strength_black > 0.0f) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, overlay.black_texture, 0, 0, 0.0F, 0.0F, width, height, width, height, ARGB.white(overlay.strength_black));
        }
    }
}

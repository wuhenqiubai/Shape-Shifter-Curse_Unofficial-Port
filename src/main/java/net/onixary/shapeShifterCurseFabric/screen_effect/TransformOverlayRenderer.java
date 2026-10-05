package net.onixary.shapeShifterCurseFabric.screen_effect;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;

/**
 * 变身叠加层的**渲染**部分（纯客户端）。
 *
 * <p>从 {@link TransformOverlay} 拆出来 —— 原因是那边的 {@code renderHud(GuiGraphics)} 方法签名含客户端类，
 * 而该类在专用服务端也会被加载（{@code onInitialize} 调 {@code TransformOverlay.INSTANCE.init()}），
 * 链接期解析签名会拉起 {@code GuiGraphics} → {@code Screen}，导致服务端启动失败。
 * 状态仍留在 {@link TransformOverlay}，本类只读它并绘制。</p>
 */
@Environment(EnvType.CLIENT)
public final class TransformOverlayRenderer {

    private static boolean hudRegistered = false;

    private TransformOverlayRenderer() {
    }

    /**
     * 1.21.11 迁移：原即时渲染 API（RenderSystem.setShader / BufferUploader / Tesselator）已全部移除。
     * 改为惰性注册 Fabric HudRenderCallback，在 HUD 渲染阶段用 GuiGraphics + RenderPipelines.GUI_TEXTURED 绘制，
     * 这样叠加层能正确显示在 HUD 之上（而非像旧的 GameRenderer ordinal=0 注入那样渲染在世界之前）。
     * 保留无参签名以兼容 GameRendererMixin 的调用方式。
     */
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
            // 1.21.11 恢复黑屏渐变（无 shader，iris 兼容）：退出时每帧衰减至透明后停止，
            // 避免 setEnableOverlay(false) 后黑屏瞬变消失
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

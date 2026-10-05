package net.onixary.shapeShifterCurseFabric.screen_effect;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

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
     * 保留无参签名以兼容 GameRendererMixin 的调用方式。
     *
     * <p><b>26.1 适配</b>：Fabric API 已移除 {@code HudRenderCallback}，且 {@code HudElementRegistry}
     * 不再是事件容器（{@code EVENT} 字段没了），改为按 {@link Identifier} 注册的 HUD 层注册表；
     * 回调接口是 {@code (GuiGraphicsExtractor, DeltaTracker)} 形状的 @FunctionalInterface。
     * 同时 GUI 绘制类由 {@code GuiGraphics} 更名为 {@code GuiGraphicsExtractor}（{@code blit} 签名不变）。</p>
     */
    public static void render() {
        ensureHudRegistered();
    }

    private static void ensureHudRegistered() {
        if (hudRegistered) {
            return;
        }
        hudRegistered = true;
        // 用 addLast：不继承任何 render condition，语义最接近旧的 HudRenderCallback（始终渲染）。
        // 若希望此覆盖层随 F1/hideGui 一起隐藏，改用
        //   HudElementRegistry.attachElementAfter(VanillaHudElements.SUBTITLES, id, cb)
        // （attach* 会继承锚点层的 render condition）。
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "transform_overlay"),
                (guiGraphics, deltaTracker) -> renderHud(guiGraphics));
    }

    private static void renderHud(GuiGraphicsExtractor guiGraphics) {
        TransformOverlay overlay = TransformOverlay.INSTANCE;
        if (!overlay.enableOverlay) {
            // 恢复黑屏渐变（无 shader，iris 兼容）：退出时每帧衰减至透明后停止，
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

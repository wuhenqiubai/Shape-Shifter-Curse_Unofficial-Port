package net.onixary.shapeShifterCurseFabric.screen_effect;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

public final class TransformOverlay {
    public static final TransformOverlay INSTANCE = new TransformOverlay();
    private final Identifier nausea_texture = Identifier.fromNamespaceAndPath(MOD_ID, "textures/overlay/nausea_black.png");
    private final Identifier black_texture = Identifier.fromNamespaceAndPath(MOD_ID, "textures/overlay/black.png");

    private boolean enableOverlay = false;
    private float strength_nausea = 0.0f;
    private float strength_black = 0.0f;

    private boolean hudRegistered = false;

    public void init() {
        enableOverlay = false;
        strength_nausea = 0.0f;
        strength_black = 0.0f;
    }

    /**
     * 1.21.11 迁移：原即时渲染 API（RenderSystem.setShader / BufferUploader / Tesselator）已全部移除。
     * 改为惰性注册 Fabric HudRenderCallback，在 HUD 渲染阶段用 GuiGraphics + RenderPipelines.GUI_TEXTURED 绘制，
     * 这样叠加层能正确显示在 HUD 之上（而非像旧的 GameRenderer ordinal=0 注入那样渲染在世界之前）。
     * 此方法保留无参签名以兼容 GameRendererMixin 的调用。
     */
    @Environment(EnvType.CLIENT)
    public void render() {
        ensureHudRegistered();
    }

    @Environment(EnvType.CLIENT)
    private void ensureHudRegistered() {
        if (hudRegistered) {
            return;
        }
        hudRegistered = true;
        HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> this.renderHud(guiGraphics));
    }

    @Environment(EnvType.CLIENT)
    private void renderHud(GuiGraphics guiGraphics) {
        if (!enableOverlay) {
            // 1.21.11 恢复黑屏渐变（无 shader，iris 兼容）：退出时每帧衰减至透明后停止，
            // 避免 setEnableOverlay(false) 后黑屏瞬变消失
            if (strength_black <= 0.01f && strength_nausea <= 0.01f) {
                return;
            }
            strength_black *= 0.85f;
            strength_nausea *= 0.85f;
        } else if (strength_black <= 0.01f && strength_nausea <= 0.01f) {
            return;
        }
        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();
        if (strength_nausea > 0.0f) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, nausea_texture, 0, 0, 0.0F, 0.0F, width, height, width, height, ARGB.white(strength_nausea));
        }
        if (strength_black > 0.0f) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, black_texture, 0, 0, 0.0F, 0.0F, width, height, width, height, ARGB.white(strength_black));
        }
    }

    public void setEnableOverlay(boolean enableOverlay) {
        this.enableOverlay = enableOverlay;
    }

    public void setNauesaStrength(float strength) {
        this.strength_nausea = Mth.clamp(strength, 0.0f, 1.0f);
    }

    public void setBlackStrength(float strength) {
        this.strength_black = Mth.clamp(strength, 0.0f, 1.0f);
    }
}

package net.onixary.shapeShifterCurseFabric.screen_effect;

import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

public final class TransformOverlay {
    public static final TransformOverlay INSTANCE = new TransformOverlay();
    final Identifier nausea_texture = Identifier.fromNamespaceAndPath(MOD_ID, "textures/overlay/nausea_black.png");
    final Identifier black_texture = Identifier.fromNamespaceAndPath(MOD_ID, "textures/overlay/black.png");

    boolean enableOverlay = false;
    float strength_nausea = 0.0f;
    float strength_black = 0.0f;

    public void init() {
        enableOverlay = false;
        strength_nausea = 0.0f;
        strength_black = 0.0f;
    }

    // 渲染方法（render / ensureHudRegistered / renderHud）连同 hudRegistered 字段已由 1.21.11 侧
    // 整体移入 TransformOverlayRenderer —— GameRendererMixin 现在直接调 TransformOverlayRenderer.render()，
    // 本类只保留状态（也正是本类注释里说的「把带客户端签名的方法移出」）。
    // 若把 26.1 侧的旧实现留着，会引用 1.21.11 已删除的 hudRegistered 字段与 ARGB import，编译失败。

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

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

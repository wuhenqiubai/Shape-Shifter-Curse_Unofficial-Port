package net.onixary.shapeShifterCurseFabric.mana;

import net.minecraft.client.gui.GuiGraphics;

public interface IManaRender {
    default boolean OverrideInstinctBar() {
        return false;
    }
    void render(GuiGraphics context, float tickDelta);
}

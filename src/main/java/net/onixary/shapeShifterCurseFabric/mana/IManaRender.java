package net.onixary.shapeShifterCurseFabric.mana;


import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface IManaRender {
    default boolean OverrideInstinctBar() {
        return false;
    }
    void render(GuiGraphicsExtractor context, float tickDelta);
}

package net.onixary.shapeShifterCurseFabric.custom_ui.ui_part;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class ScaleMultilineTextWidget extends MultiLineTextWidget {
    private final float Scale;
    protected boolean shadow;

    public ScaleMultilineTextWidget shadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }
    public boolean shadow() {
        return this.shadow;
    }

    public ScaleMultilineTextWidget(int x, int y, Component message, Font textRenderer, float Scale) {
        super(x, y, message, textRenderer);
        this.Scale = Scale;
        this.shadow = false;
    }

    public @NonNull MultiLineTextWidget setMaxWidth(int maxWidth) {
        super.setMaxWidth(Math.round(maxWidth * (1 / this.Scale)));
        return this;
    }

    public int getWidth() {
        return (int) (super.getWidth() * this.Scale);
    }

    public int getHeight() {
        return (int) (super.getHeight() * this.Scale);
    }
}
package net.onixary.shapeShifterCurseFabric.custom_ui.ui_part;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.network.chat.Component;

import java.util.Objects;

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

    public MultiLineTextWidget setMaxWidth(int maxWidth) {
        super.setMaxWidth(Math.round(maxWidth * (1 / this.Scale)));
        return this;
    }

    public int getWidth() {
        return (int) (super.getWidth() * this.Scale);
    }

    public int getHeight() {
        return (int) (super.getHeight() * this.Scale);
    }

    public void renderButton(GuiGraphics context, int mouseX, int mouseY, float delta) {
        MultiLineLabel multilineText = this.cache.getValue(this.getFreshCacheKey());
        int i = this.getX();
        int j = this.getY();
        Objects.requireNonNull(this.getFont());
        int k = Math.round(9 * this.Scale);
        int l = this.getColor();
        if (this.centered) {
            multilineText.renderCentered(context, i + this.getWidth() / 2, j, k, l);
        } else {
            if(this.shadow){
                multilineText.renderLeftAligned(context, i, j, k, l);
            }
            else{
                multilineText.renderLeftAlignedNoShadow(context, i, j, k, l);
            }
        }
    }
}
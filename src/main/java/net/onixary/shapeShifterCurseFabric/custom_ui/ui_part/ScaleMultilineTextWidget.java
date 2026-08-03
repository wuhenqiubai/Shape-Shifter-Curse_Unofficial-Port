package net.onixary.shapeShifterCurseFabric.custom_ui.ui_part;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;
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

    @Override
    public void renderWidget(@NonNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        // 1.21.11 文字渲染走 GuiTextRenderState（延迟提交），字形缩放只能由保存的 2D pose 决定：
        // 在 widget 原点处缩放 GUI pose Scale 倍，使 vanilla MultiLineTextWidget 文字真正缩小 Scale 倍显示。
        Matrix3x2fStack pose = context.pose();
        pose.pushMatrix();
        pose.translate(this.getX(), this.getY());
        pose.scale(this.Scale, this.Scale);
        pose.translate(-this.getX(), -this.getY());
        super.renderWidget(context, mouseX, mouseY, delta);
        pose.popMatrix();
    }
}
package net.onixary.shapeShifterCurseFabric.custom_ui.ui_part;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;


@Environment(EnvType.CLIENT)
public class ScaleTextRenderer extends Font {
    public float Scale = 1.0f;
    private static final Vector3f FORWARD_SHIFT = new Vector3f(0.0F, 0.0F, 0.03F);

    public ScaleTextRenderer(@NotNull Font textRenderer) {
        super(textRenderer.fonts, textRenderer.filterFishyGlyphs);
    }

    public int drawInBatch(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, MultiBufferSource vertexConsumers, DisplayMode layerType, int backgroundColor, int light) {
        return this.drawInBatch(text, x, y, color, shadow, matrix, vertexConsumers, layerType, backgroundColor, light, this.isBidirectional());
    }

    public int drawInBatch(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, MultiBufferSource vertexConsumers, DisplayMode layerType, int backgroundColor, int light, boolean rightToLeft) {
        return this.drawInternal(text, x, y, color, shadow, matrix, vertexConsumers, layerType, backgroundColor, light, rightToLeft);
    }

    public int drawInBatch(Component text, float x, float y, int color, boolean shadow, Matrix4f matrix, MultiBufferSource vertexConsumers, DisplayMode layerType, int backgroundColor, int light) {
        return this.drawInBatch(text.getVisualOrderText(), x, y, color, shadow, matrix, vertexConsumers, layerType, backgroundColor, light);
    }

    public int drawInBatch(FormattedCharSequence text, float x, float y, int color, boolean shadow, Matrix4f matrix, MultiBufferSource vertexConsumers, DisplayMode layerType, int backgroundColor, int light) {
        return this.drawInternal(text, x, y, color, shadow, matrix, vertexConsumers, layerType, backgroundColor, light);
    }

    private int drawInternal(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, MultiBufferSource vertexConsumers, DisplayMode layerType, int backgroundColor, int light, boolean mirror) {
        float RScale = 1.0f / this.Scale;
        if (mirror) {
            text = this.bidirectionalShaping(text);
        }
        color = adjustColor(color);
        Matrix4f matrix4f = new Matrix4f(matrix);
        matrix.scale(this.Scale);
        matrix4f.scale(this.Scale);
        if (shadow) {
            this.renderText(text, x * RScale, y * RScale, color, true, matrix, vertexConsumers, layerType, backgroundColor, light);
            matrix4f.translate(FORWARD_SHIFT);
        }

        x = this.renderText(text, x * RScale, y * RScale, color, false, matrix4f, vertexConsumers, layerType, backgroundColor, light);
        matrix.scale(RScale);
        matrix4f.scale(RScale);
        return (int)x + (shadow ? 1 : 0);
    }

    private int drawInternal(FormattedCharSequence text, float x, float y, int color, boolean shadow, Matrix4f matrix, MultiBufferSource vertexConsumerProvider, DisplayMode layerType, int backgroundColor, int light) {
        float RScale = 1.0f / this.Scale;
        color = adjustColor(color);
        Matrix4f matrix4f = new Matrix4f(matrix);
        matrix.scale(this.Scale);
        matrix4f.scale(this.Scale);
        if (shadow) {
            this.renderText(text, x * RScale, y * RScale, color, true, matrix, vertexConsumerProvider, layerType, backgroundColor, light);
            matrix4f.translate(FORWARD_SHIFT);
        }

        x = this.renderText(text, x * RScale, y * RScale, color, false, matrix4f, vertexConsumerProvider, layerType, backgroundColor, light);
        matrix.scale(RScale);
        matrix4f.scale(RScale);
        return (int)x + (shadow ? 1 : 0);
    }

    public int wordWrapHeight(String text, int maxWidth) {
        return Math.round(9 * this.Scale) * this.getSplitter().splitLines(text, maxWidth, Style.EMPTY).size();
    }

    public int wordWrapHeight(FormattedText text, int maxWidth) {
        return Math.round(9 * this.Scale) * this.getSplitter().splitLines(text, maxWidth, Style.EMPTY).size();
    }

    public int width(String text) {
        return Mth.ceil(this.getSplitter().stringWidth(text) * this.Scale);
    }

    public int width(FormattedText text) {
        return Mth.ceil(this.getSplitter().stringWidth(text) * this.Scale);
    }

    public int width(FormattedCharSequence text) {
        return Mth.ceil(this.getSplitter().stringWidth(text) * this.Scale);
    }

    public String plainSubstrByWidth(String text, int maxWidth, boolean backwards) {
        return backwards ? this.getSplitter().plainTailByWidth(text, (int) (maxWidth * (1.0f / this.Scale)), Style.EMPTY) : this.getSplitter().plainHeadByWidth(text, maxWidth, Style.EMPTY);
    }

    public String plainSubstrByWidth(String text, int maxWidth) {
        return this.getSplitter().plainHeadByWidth(text, (int) (maxWidth * (1.0f / this.Scale)), Style.EMPTY);
    }

    public FormattedText substrByWidth(FormattedText text, int width) {
        return this.getSplitter().headByWidth(text, width, Style.EMPTY);
    }
}
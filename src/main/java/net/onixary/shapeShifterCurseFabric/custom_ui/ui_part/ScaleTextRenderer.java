package net.onixary.shapeShifterCurseFabric.custom_ui.ui_part;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.onixary.shapeShifterCurseFabric.mixin.FontAccessor;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;


@Environment(EnvType.CLIENT)
public class ScaleTextRenderer extends Font {
    public float Scale = 1.0f;

	public ScaleTextRenderer(@NotNull Font textRenderer) {
        super(((FontAccessor) textRenderer).getProvider());
    }

    public int width(@NonNull FormattedText text) {
        return Mth.ceil(this.getSplitter().stringWidth(text) * this.Scale);
    }

    public int width(@NonNull FormattedCharSequence text) {
        return Mth.ceil(this.getSplitter().stringWidth(text) * this.Scale);
    }

    public @NotNull String plainSubstrByWidth(String text, int maxWidth, boolean backwards) {
        return backwards ? this.getSplitter().plainTailByWidth(text, (int) (maxWidth * (1.0f / this.Scale)), Style.EMPTY) : this.getSplitter().plainHeadByWidth(text, maxWidth, Style.EMPTY);
    }

    public @NotNull String plainSubstrByWidth(String text, int maxWidth) {
        return this.getSplitter().plainHeadByWidth(text, (int) (maxWidth * (1.0f / this.Scale)), Style.EMPTY);
    }

    public @NotNull FormattedText substrByWidth(FormattedText text, int width) {
        return this.getSplitter().headByWidth(text, width, Style.EMPTY);
    }
}
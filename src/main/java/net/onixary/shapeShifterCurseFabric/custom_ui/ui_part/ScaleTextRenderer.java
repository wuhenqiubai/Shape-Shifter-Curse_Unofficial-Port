package net.onixary.shapeShifterCurseFabric.custom_ui.ui_part;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Field;


@Environment(EnvType.CLIENT)
public class ScaleTextRenderer extends Font {
    public float Scale = 1.0f;

	public ScaleTextRenderer(@NotNull Font textRenderer) {
        super(extractProvider(textRenderer));
    }

    private static Font.Provider extractProvider(Font font) {
        try {
            Field field = Font.class.getDeclaredField("provider");
            field.setAccessible(true);
            return (Font.Provider) field.get(font);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract Font provider", e);
        }
    }

    public int width(@NonNull FormattedText text) {
        return Mth.ceil(this.getSplitter().stringWidth(text) * this.Scale);
    }

    public int width(@NonNull FormattedCharSequence text) {
        return Mth.ceil(this.getSplitter().stringWidth(text) * this.Scale);
    }

    public @NonNull FormattedText substrByWidth(@NonNull FormattedText text, int width) {
        return this.getSplitter().headByWidth(text, width, Style.EMPTY);
    }
}
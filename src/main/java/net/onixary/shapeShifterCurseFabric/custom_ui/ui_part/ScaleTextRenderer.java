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
        // 1.21.11: Font.provider 字段运行时是 intermediary 混淆名，反射 getDeclaredField("provider") 找不到
        // （NoSuchFieldException）。改用 mixin @Accessor（处理器会映射字段名）。
        super(((FontAccessor) textRenderer).getProvider());
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
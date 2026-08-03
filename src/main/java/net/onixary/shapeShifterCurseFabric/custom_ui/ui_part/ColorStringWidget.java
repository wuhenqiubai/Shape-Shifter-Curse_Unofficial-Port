package net.onixary.shapeShifterCurseFabric.custom_ui.ui_part;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

/**
 * 1.21.11 的 StringWidget 通过 ActiveTextCollector 渲染，文本颜色固定为白色（ARGB.white(opacity)），
 * 无 setTextColor，浅色背景（如书页纹理）上不可见。
 * 本类改用 GuiGraphics.drawString 直接渲染，支持 setColor，恢复 1.21.1 TextWidget.setTextColor 语义。
 */
public class ColorStringWidget extends StringWidget {
    private int color = 0xFFFFFF;

    public ColorStringWidget(int x, int y, int width, int height, Component message, Font font) {
        super(x, y, width, height, message, font);
    }

    public ColorStringWidget setColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    public void renderWidget(@NonNull GuiGraphics guiGraphics, int i, int j, float f) {
        // 与 StringWidget.visitLines 的垂直居中一致：文字顶 = y + (height - 9) / 2
        int y = this.getY() + (this.getHeight() - 9) / 2;
        guiGraphics.drawString(this.getFont(), this.getMessage().getVisualOrderText(), this.getX(), y, this.color);
    }
}

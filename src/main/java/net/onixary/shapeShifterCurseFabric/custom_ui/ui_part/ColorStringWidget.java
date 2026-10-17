package net.onixary.shapeShifterCurseFabric.custom_ui.ui_part;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

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
        int y = this.getY() + (this.getHeight() - 9) / 2;
        guiGraphics.drawString(this.getFont(), this.getMessage().getVisualOrderText(), this.getX(), y, this.color);
    }
}

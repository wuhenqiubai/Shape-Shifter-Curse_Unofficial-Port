package net.onixary.shapeShifterCurseFabric.custom_ui.ui_part;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

public class FCS_ButtonWidget extends Button {
    public final Identifier WIDGETS_TEXTURE = ShapeShifterCurseFabric.identifier("textures/gui/form_color_select_menu_part.png");
    public int TEXTURE_X = 0;


    public FCS_ButtonWidget(int x, int y, Component message, OnPress onPress, CreateNarration narrationSupplier, int TEXTURE_X) {
        super(x, y, 15, 15, message, onPress, narrationSupplier);
        this.TEXTURE_X = TEXTURE_X;
    }

    private int getTextureY() {
        int i = 0;
        if (!this.active) {
            i = 2;
        } else if (this.isHoveredOrFocused()) {
            i = 1;
        }
        return i * 15;
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        Minecraft minecraftClient = Minecraft.getInstance();
        context.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        context.blit(WIDGETS_TEXTURE, this.getX(), this.getY(), TEXTURE_X, this.getTextureY(), 15, 15, 45, 45);
        context.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = this.active ? 16777215 : 10526880;
        this.renderString(context, minecraftClient.font, i | Mth.ceil(this.alpha * 255.0F) << 24);
    }
}
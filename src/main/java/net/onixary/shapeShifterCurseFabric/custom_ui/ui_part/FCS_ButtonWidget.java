package net.onixary.shapeShifterCurseFabric.custom_ui.ui_part;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
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
    protected void renderContents(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // renderWidget 在 1.21.11 为 final，改重写 renderContents
        // RenderSystem.enableBlend()/enableDepthTest() 已移除，RenderPipeline 自带渲染状态
        int color = ARGB.white(this.alpha);
        context.blit(RenderPipelines.GUI_TEXTURED, WIDGETS_TEXTURE, this.getX(), this.getY(),
                TEXTURE_X, this.getTextureY(), 15, 15, 0, 0, 45, 45, color);
        this.renderDefaultLabel(context.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.NONE));
    }
}
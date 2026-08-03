package net.onixary.shapeShifterCurseFabric.custom_ui.ui_part;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

import java.util.function.Consumer;

public class SimpleIntSliderWidget extends AbstractSliderButton {
    private static final Identifier TEXTURE = Identifier.parse("textures/gui/slider.png");
	public final int minValue;
    public final int maxValue;

    public int intValue = 0;
    public Consumer<SimpleIntSliderWidget> onChanged = null;


    public SimpleIntSliderWidget(int x, int y, int width, int height, Component text, double value, int minValue, int maxValue) {
        super(x, y, width, height, text, value);
        this.minValue = minValue;
        this.maxValue = maxValue;
        if (this.maxValue == this.minValue) {
            throw new IllegalArgumentException("Max value must be greater than min value"); // 这必须得throw了 否则会在setIntValue里报错
        }
    }

    @Override
    protected void updateMessage() {
    }

    @Override
    protected void applyValue() {
        double value = this.value;
        this.intValue = (int) (value * (maxValue - minValue) + minValue);
        if (this.onChanged != null) {
            this.onChanged.accept(this);
        }
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // RenderSystem.enableBlend()/defaultBlendFunc()/enableDepthTest() 已移除，RenderPipeline 自带渲染状态
        int textureY = this.active ? (this.isHovered() ? 1 : 0) : 2;
        int color = ARGB.white(this.alpha);

        // 1.21.11: 13参 blit 语义 (pipeline,id,x,y,u,v,w,h,uWidth,vHeight,texW,texH,color)，uWidth/vHeight 是 UV 区域尺寸（=显示宽高），不能为 0
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX(), this.getY(), 0, textureY * 20,
                this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight(), 200, 60, color);

        int sliderX = this.getX() + (int) (this.value * (double) (this.getWidth() - 8));
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, sliderX, this.getY(), 0, textureY * 20 + 40,
                8, this.getHeight(), 8, this.getHeight(), 200, 60, color);

        this.renderScrollingStringOverContents(
                context.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.NONE),
                this.getMessage(), 2);
    }

    public void setIntValue(int value) {
        this.value = (value - minValue) / (double) (maxValue - minValue);
        this.applyValue();
    }

    public int getIntValue() {
        return this.intValue;
    }
}
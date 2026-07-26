package net.onixary.shapeShifterCurseFabric.custom_ui.ui_part;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

public class SimpleIntSliderWidget extends AbstractSliderButton {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("textures/gui/slider.png");
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
        Minecraft minecraftClient = Minecraft.getInstance();
        context.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        int textureY = this.active ? (this.isHovered() ? 1 : 0) : 2;
        context.blit(TEXTURE, this.getX(), this.getY(), 0, textureY * 20, this.getWidth(), this.getHeight(), 200, 60);

        int sliderX = this.getX() + (int) (this.value * (double) (this.getWidth() - 8));
        context.blit(TEXTURE, sliderX, this.getY(), 0, textureY * 20 + 40, 8, this.getHeight(), 200, 60);

        context.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = this.active ? 16777215 : 10526880;
        this.renderScrollingString(context, minecraftClient.font, 2, i | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    public void setIntValue(int value) {
        this.value = (value - minValue) / (double) (maxValue - minValue);
        this.applyValue();
    }

    public int getIntValue() {
        return this.intValue;
    }
}
package net.onixary.shapeShifterCurseFabric.util.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

// 拓展的护盾画法 为了极致的速度 尽量少添加贴图
public interface ISprite {
    public Identifier getTextureID();

    public int getTextureImgWidth();

    public int getTextureImgHeight();

    public int getTextureX();

    public int getTextureY();

    public int getTextureWidth();

    public int getTextureHeight();

    public default void draw(GuiGraphics context, int x, int y) {
        this.draw(context, x, y, 0, 0, 0, getTextureWidth(), getTextureHeight());
    }

    public default void draw(GuiGraphics context, int x, int y, int z, int u, int v, int width, int height) {
        this.draw(context, x, x + width, y, y + height, z, width, height, u, v);
    }

    public default void draw(GuiGraphics context, int x1, int x2, int y1, int y2, int z, int regionWidth, int regionHeight, float u, float v) {
        context.blit(RenderPipelines.GUI_TEXTURED, getTextureID(), x1, y1,
                getTextureX() + u, getTextureY() + v,
                x2 - x1, y2 - y1, regionWidth, regionHeight,
                getTextureImgWidth(), getTextureImgHeight(), -1);
    }
}

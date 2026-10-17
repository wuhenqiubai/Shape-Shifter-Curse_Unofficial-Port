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
        context.blit(RenderPipelines.GUI_TEXTURED, getTextureID(), x, y,
                getTextureX() + u, getTextureY() + v,
                width, height, width, height,
                getTextureImgWidth(), getTextureImgHeight(), -1);
    }
}

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

    // 1.21.11：blit 只保留带 RenderPipeline 的重载，且不再有 z 参数 ——
    // 旧写法 blit(id, x, y, z, u, v, w, h, texW, texH) 会把 id 当成 pipeline 而编译失败。
    // 对应转换：blit(GUI_TEXTURED, id, x, y, u, v, w, h, w, h, texW, texH, -1)
    //（uWidth/vHeight 是 UV 区域尺寸，此处取显示宽高；末位 -1 表示不染色。）
    public default void draw(GuiGraphics context, int x, int y, int z, int u, int v, int width, int height) {
        context.blit(RenderPipelines.GUI_TEXTURED, getTextureID(), x, y,
                getTextureX() + u, getTextureY() + v,
                width, height, width, height,
                getTextureImgWidth(), getTextureImgHeight(), -1);
    }
}

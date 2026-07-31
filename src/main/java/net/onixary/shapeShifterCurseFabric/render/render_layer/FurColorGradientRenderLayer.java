package net.onixary.shapeShifterCurseFabric.render.render_layer;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.joml.Vector4f;

/**
 * TODO: 1.21.11 移除 Satin，fur gradient shader（ShapeShifterCurseFabricClient.getFurGradientShader）
 * 无法复刻。此 RenderType 退化为普通 translucent 渲染（颜色渐变效果暂不可用）。
 * 复刻方向：1.21.11 自定义 core shader 需走 RenderPipeline / PostChain。
 */
public abstract class FurColorGradientRenderLayer {
    public static RenderType getFurLayer(Identifier texture, Vector4f startColor, Vector4f endColor) {
        return RenderTypes.entityTranslucent(texture);
    }
}

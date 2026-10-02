package net.onixary.shapeShifterCurseFabric.render.render_layer;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.joml.Vector4f;

/**
 * [未启用，勿再当作待办] 1.21.11 移除 Satin，fur gradient shader
 * （ShapeShifterCurseFabricClient.getFurGradientShader）无法复刻。
 * 此法退化为普通 translucent 渲染（颜色渐变效果不可用）。
 *
 * 关键事实：两个分支上都无调用点，整套从未启用；且 1.21.11 恢复需自定义 core shader，
 * 会与光影包（Iris 等）冲突，故不采用着色器方案。如需恢复请先确认调用方。
 */
public abstract class FurColorGradientRenderLayer {
    public static RenderType getFurLayer(Identifier texture, Vector4f startColor, Vector4f endColor) {
        return RenderTypes.entityTranslucent(texture);
    }
}

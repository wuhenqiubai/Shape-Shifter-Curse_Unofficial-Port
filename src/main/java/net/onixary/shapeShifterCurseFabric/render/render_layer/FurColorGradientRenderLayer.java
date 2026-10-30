package net.onixary.shapeShifterCurseFabric.render.render_layer;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.joml.Vector4f;

public abstract class FurColorGradientRenderLayer {
    public static RenderType getFurLayer(Identifier texture, Vector4f startColor, Vector4f endColor) {
        return RenderTypes.entityTranslucent(texture);
    }
}

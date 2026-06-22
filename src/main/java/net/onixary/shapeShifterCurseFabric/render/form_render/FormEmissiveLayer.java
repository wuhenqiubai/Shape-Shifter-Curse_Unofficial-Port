package net.onixary.shapeShifterCurseFabric.render.form_render;

import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.renderer.GeoObjectRenderer;

/**
 * Custom render layer that re-renders the Geo model with the form's
 * emissive/fullbright texture at maximum light. Replaces the second
 * {@code formRenderer.render()} call in FormRenderFeature.
 */
public class FormEmissiveLayer extends GeoRenderLayer<FormAnimatable> {

    public FormEmissiveLayer(GeoRenderer<FormAnimatable> renderer) {
        super(renderer);
    }

    @Override
    public void render(net.minecraft.client.util.math.MatrixStack poseStack, FormAnimatable animatable,
                       BakedGeoModel model, RenderLayer renderType,
                       net.minecraft.client.render.VertexConsumerProvider bufferSource,
                       net.minecraft.client.render.VertexConsumer buffer,
                       float partialTick, int packedLight, int packedOverlay) {
        // Get the FormModel to access per-form emissive texture
        if (!(getRenderer() instanceof GeoObjectRenderer<FormAnimatable> objRenderer)) return;
        if (!(objRenderer.getGeoModel() instanceof FormModel formModel)) return;

        Identifier emissiveTexture = formModel.getFullbrightTextureResource(animatable);
        if (emissiveTexture == null) return;

        RenderLayer emissiveLayer = RenderLayer.getEntityTranslucentEmissive(emissiveTexture);
        getRenderer().reRender(model, poseStack, bufferSource, animatable, emissiveLayer,
            bufferSource.getBuffer(emissiveLayer), partialTick,
            LightmapTextureManager.MAX_LIGHT_COORDINATE, packedOverlay, 0xFFFFFFFF);
    }
}

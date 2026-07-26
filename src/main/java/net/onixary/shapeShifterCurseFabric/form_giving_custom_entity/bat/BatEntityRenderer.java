package net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.bat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.BatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ambient.Bat;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

public class BatEntityRenderer extends MobRenderer<Bat, net.minecraft.client.model.BatModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/entity/mob/t_bat.png");

    public BatEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new BatModel(context.bakeLayer(ModelLayers.BAT)), 0.25F);
    }

    public ResourceLocation getTextureLocation(Bat batEntity) {
        return TEXTURE;
    }

    protected void scale(Bat batEntity, PoseStack matrixStack, float f) {
        matrixStack.scale(0.35F, 0.35F, 0.35F);
    }

    protected void setupRotations(Bat batEntity, PoseStack matrixStack, float f, float g, float h, float scale) {
        if (batEntity.isResting()) {
            matrixStack.translate(0.0F, -0.1F, 0.0F);
        } else {
            matrixStack.translate(0.0F, Mth.cos(f * 0.3F) * 0.1F, 0.0F);
        }

        super.setupRotations(batEntity, matrixStack, f, g, h, scale);
    }
}
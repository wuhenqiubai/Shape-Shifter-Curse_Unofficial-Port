package net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.bat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ambient.BatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.BatRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ambient.Bat;
import org.jspecify.annotations.NonNull;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

public class BatEntityRenderer extends MobRenderer<Bat, BatRenderState, BatModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/mob/t_bat.png");

    public BatEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new BatModel(context.bakeLayer(ModelLayers.BAT)), 0.25F);
    }

    public @NonNull Identifier getTextureLocation(@NonNull BatRenderState state) {
        return TEXTURE;
    }

    @Override
    protected void scale(BatRenderState state, PoseStack poseStack) {
        poseStack.scale(0.35F, 0.35F, 0.35F);
    }

    @Override
    protected void setupRotations(BatRenderState state, PoseStack poseStack, float f, float g) {
        if (state.isResting) {
            poseStack.translate(0.0F, -0.1F, 0.0F);
        } else {
            poseStack.translate(0.0F, Mth.cos(g * 0.3F) * 0.1F, 0.0F);
        }

        super.setupRotations(state, poseStack, f, g);
    }
}
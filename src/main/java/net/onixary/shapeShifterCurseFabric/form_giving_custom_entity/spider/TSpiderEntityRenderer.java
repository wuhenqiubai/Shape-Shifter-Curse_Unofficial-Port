package net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.spider;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

@Environment(EnvType.CLIENT)
public class TSpiderEntityRenderer extends MobRenderer<TransformativeSpiderEntity, LivingEntityRenderState, SpiderModel> {
	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/mob/t_spider.png");

	public TSpiderEntityRenderer(EntityRendererProvider.Context context) {
		super(context, new SpiderModel(context.bakeLayer(ModelLayers.SPIDER)), 0.8F);
	}

	@Override
	public @NonNull LivingEntityRenderState createRenderState() {
		return new LivingEntityRenderState();
	}

	@Override
	public @NonNull Identifier getTextureLocation(@NonNull LivingEntityRenderState livingEntityRenderState) {
		return TEXTURE;
	}

	@Override
	protected void scale(LivingEntityRenderState state, PoseStack poseStack) {
		poseStack.scale(0.5f, 0.5f, 0.5f);
	}
}
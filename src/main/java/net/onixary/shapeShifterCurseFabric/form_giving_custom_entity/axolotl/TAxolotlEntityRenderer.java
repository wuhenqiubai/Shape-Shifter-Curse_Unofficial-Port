package net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.axolotl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.animal.axolotl.AxolotlModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.AxolotlRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import org.jspecify.annotations.NonNull;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

@Environment(EnvType.CLIENT)
public class TAxolotlEntityRenderer extends MobRenderer<Axolotl, AxolotlRenderState, AxolotlModel> {

	// 1. 删除了整个 TEXTURES 静态映射的定义

	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/mob/t_axolotl.png");

	public TAxolotlEntityRenderer(EntityRendererProvider.Context context) {
		super(context, new AxolotlModel(context.bakeLayer(ModelLayers.AXOLOTL)), 0.5F);
	}

	@Override
	public @NonNull Identifier getTextureLocation(@NonNull AxolotlRenderState state) {
		return TEXTURE;
	}
}
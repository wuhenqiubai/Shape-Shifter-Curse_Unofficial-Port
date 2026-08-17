package net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.ocelot;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.animal.feline.OcelotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.FelineRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.feline.Ocelot;
import org.jspecify.annotations.NonNull;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

@Environment(EnvType.CLIENT)
public class TOcelotEntityRenderer extends MobRenderer<Ocelot, FelineRenderState, OcelotModel> {
	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/mob/t_ocelot.png");

	public TOcelotEntityRenderer(EntityRendererProvider.Context context) {
		super(context, new OcelotModel(context.bakeLayer(ModelLayers.OCELOT)), 0.4F);
	}

	@Override
	public @NonNull FelineRenderState createRenderState() {
		return new FelineRenderState();
	}

    @Override
    public @NonNull Identifier getTextureLocation(@NonNull FelineRenderState state) {
		return TEXTURE;
	}
}
package net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.ocelot;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.OcelotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Ocelot;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

@Environment(EnvType.CLIENT)
public class TOcelotEntityRenderer extends MobRenderer<Ocelot, OcelotModel<Ocelot>> {
	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/entity/mob/t_ocelot.png");

	public TOcelotEntityRenderer(EntityRendererProvider.Context context) {
		super(context, new OcelotModel<>(context.bakeLayer(ModelLayers.OCELOT)), 0.4F);
	}

	@Override
    public ResourceLocation getTextureLocation(Ocelot ocelotEntity) {
		return TEXTURE;
	}
}
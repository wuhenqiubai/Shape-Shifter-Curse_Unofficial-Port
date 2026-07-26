package net.onixary.shapeShifterCurseFabric.minion.mobs;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;
import static net.onixary.shapeShifterCurseFabric.minion.MinionRegisterClient.WOLF_MINION_LAYER;

@Environment(EnvType.CLIENT)
public class AnubisWolfMinionEntityRenderer extends MobRenderer<Wolf, AnubisWolfMinionEntityModel<Wolf>> {
    private static final ResourceLocation ANUBIS_WOLF_MINION_TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/entity/mob/anubis_wolf_minion.png");

    public AnubisWolfMinionEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new AnubisWolfMinionEntityModel<>(context.bakeLayer(WOLF_MINION_LAYER)), 0.5F);
    }

    protected float getBob(Wolf wolfEntity, float f) {
        return wolfEntity.getTailAngle();
    }

    @Override
    public ResourceLocation getTextureLocation(Wolf entity) {
        return ANUBIS_WOLF_MINION_TEXTURE;
    }
}
package net.onixary.shapeShifterCurseFabric.minion.mobs;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;
import org.jetbrains.annotations.NotNull;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;
import static net.onixary.shapeShifterCurseFabric.minion.MinionRegisterClient.WOLF_MINION_LAYER;

@Environment(EnvType.CLIENT)
public class AnubisWolfMinionEntityRenderer extends MobRenderer<Wolf, WolfRenderState, AnubisWolfMinionEntityModel> {
    private static final Identifier ANUBIS_WOLF_MINION_TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/mob/anubis_wolf_minion.png");

    public AnubisWolfMinionEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new AnubisWolfMinionEntityModel(context.bakeLayer(WOLF_MINION_LAYER)), 0.5F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(Wolf entity) {
        return ANUBIS_WOLF_MINION_TEXTURE;
    }
}
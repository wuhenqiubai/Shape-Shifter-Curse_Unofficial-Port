package net.onixary.shapeShifterCurseFabric.minion.mobs;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;
import static net.onixary.shapeShifterCurseFabric.minion.MinionRegisterClient.WOLF_MINION_LAYER;

@Environment(EnvType.CLIENT)
public class AnubisWolfMinionEntityRenderer extends MobRenderer<Wolf, WolfRenderState, AnubisWolfMinionEntityModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/mob/anubis_wolf_minion.png");

    @Override
    public @NonNull WolfRenderState createRenderState() {
        return new WolfRenderState();
    }

    public AnubisWolfMinionEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new AnubisWolfMinionEntityModel(context.bakeLayer(WOLF_MINION_LAYER)), 0.5F);
    }

    @Override
    public @NotNull Identifier getTextureLocation(WolfRenderState entity) {
        return TEXTURE;
    }
}
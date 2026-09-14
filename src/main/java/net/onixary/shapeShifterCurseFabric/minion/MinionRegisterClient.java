package net.onixary.shapeShifterCurseFabric.minion;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.onixary.shapeShifterCurseFabric.minion.mobs.AnubisWolfMinionEntity;
import net.onixary.shapeShifterCurseFabric.minion.mobs.AnubisWolfMinionEntityModel;
import net.onixary.shapeShifterCurseFabric.minion.mobs.AnubisWolfMinionEntityRenderer;

@Environment(EnvType.CLIENT)
public class MinionRegisterClient {
    public static final ModelLayerLocation WOLF_MINION_LAYER = new ModelLayerLocation(AnubisWolfMinionEntity.MinionID, "main");

    public static void registerClient() {
        EntityRendererRegistry.register(MinionRegister.ANUBIS_WOLF_MINION, AnubisWolfMinionEntityRenderer::new);
        ModelLayerRegistry.registerModelLayer(WOLF_MINION_LAYER, AnubisWolfMinionEntityModel::getTexturedModelData);
    }
}

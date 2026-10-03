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

    // 26.1 起 Wolf 专属的 render state 字段不再由基类填充，需 renderer 自己从实体搬过来，
    // 否则模型里的 isAngry / isSitting / tailAngle 恒为默认值（尾巴角度固定、不随状态变化）。
    // 只填模型实际用到的那三项；texture / collarColor / bodyArmorItem 等本 minion 用不到。
    @Override
    public void extractRenderState(Wolf entity, WolfRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isAngry = entity.isAngry();
        state.isSitting = entity.isInSittingPose();
        state.tailAngle = entity.getTailAngle();
    }
}
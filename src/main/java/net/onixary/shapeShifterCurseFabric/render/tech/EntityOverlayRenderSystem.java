package net.onixary.shapeShifterCurseFabric.render.tech;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.onixary.shapeShifterCurseFabric.status_effects.RegOtherStatusEffects;
import software.bernie.geckolib.renderer.GeoObjectRenderer;

import java.util.ArrayList;

public class EntityOverlayRenderSystem {
    public static EmptyAnimatable EmptyAnimatable = new EmptyAnimatable();

    public static abstract class OverlayData {
        public abstract boolean canRender(Entity entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light);
        public abstract void render(Entity entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light);
    }

    public static ArrayList<OverlayData> overlayDataList = new ArrayList<OverlayData>();

    static {
        overlayDataList.add(new OverlayData() {
            private static CocoonModel cocoonModel = new CocoonModel();
            private static GeoObjectRenderer<EmptyAnimatable> cocoonRenderer = new GeoObjectRenderer<EmptyAnimatable>(cocoonModel);

            @Override
            public boolean canRender(Entity entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
                return entity instanceof LivingEntity livingEntity && livingEntity.getEffect(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(RegOtherStatusEffects.ENTANGLED_FULL_EFFECT)) != null;
            }

            @Override
            public void render(Entity entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
                matrices.pushPose();
                matrices.translate(-0.5, -0.5, -0.5);
                cocoonRenderer.render(matrices, EmptyAnimatable, vertexConsumers, RenderType.entityTranslucent(cocoonModel.getTextureResource(EmptyAnimatable)), null, light, tickDelta);
                matrices.popPose();
            }
        });
    }

    public static void render(Entity entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
        // 保底一下 防止炸的太厉害
        matrices.pushPose();
        for (OverlayData overlayData : overlayDataList) {
            if (overlayData.canRender(entity, yaw, tickDelta, matrices, vertexConsumers, light)) {
                overlayData.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
            }
        }
        matrices.popPose();
    }
}
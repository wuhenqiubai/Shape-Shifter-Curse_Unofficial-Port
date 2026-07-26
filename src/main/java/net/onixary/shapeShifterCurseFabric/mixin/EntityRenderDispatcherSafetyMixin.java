package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Safeguard: prevents NPE when EntityRenderDispatcher has a null renderer
 * (can happen during form transitions or renderer cache mismatches).
 */
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherSafetyMixin {

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Redirect(method = "shouldRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;shouldRender(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z"))
	private boolean preventNullRendererShouldRender(EntityRenderer instance, net.minecraft.world.entity.Entity entity, Frustum frustum, double x, double y, double z) {
		if (instance == null) return false;
		return instance.shouldRender(entity, frustum, x, y, z);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
	private void preventNullRendererRender(EntityRenderer instance, net.minecraft.world.entity.Entity entity, float yaw, float tickDelta, com.mojang.blaze3d.vertex.PoseStack matrices, net.minecraft.client.renderer.MultiBufferSource vertexConsumers, int light) {
		if (instance != null) {
			instance.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Redirect(method = "getPackedLightCoords", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;getPackedLightCoords(Lnet/minecraft/world/entity/Entity;F)I"))
	private int preventNullRendererGetLight(EntityRenderer instance, net.minecraft.world.entity.Entity entity, float tickDelta) {
		if (instance == null) return 15; // 默认最大亮度
		return instance.getPackedLightCoords(entity, tickDelta);
	}
}

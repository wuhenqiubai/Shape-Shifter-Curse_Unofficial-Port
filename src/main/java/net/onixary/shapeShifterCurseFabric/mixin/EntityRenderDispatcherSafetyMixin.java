package net.onixary.shapeShifterCurseFabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherSafetyMixin {

	@Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
	private void skipNullRendererRender(Entity entity, double x, double y, double z, float yaw, float tickDelta, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
		if (((EntityRenderDispatcher) (Object) this).getRenderer(entity) == null) ci.cancel();
	}

	@Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
	private void skipNullRendererShouldRender(Entity entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
		if (((EntityRenderDispatcher) (Object) this).getRenderer(entity) == null) cir.setReturnValue(false);
	}

	@Inject(method = "getPackedLightCoords", at = @At("HEAD"), cancellable = true)
	private void skipNullRendererPackedLight(Entity entity, float tickDelta, CallbackInfoReturnable<Integer> cir) {
		if (((EntityRenderDispatcher) (Object) this).getRenderer(entity) == null) cir.setReturnValue(15);
	}
}

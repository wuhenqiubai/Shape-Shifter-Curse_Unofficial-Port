package net.onixary.shapeShifterCurseFabric.mixin;

import com.geckolib.constant.dataticket.DataTicket;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.onixary.shapeShifterCurseFabric.render.tech.EntityOverlayRenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {

	@Unique
	private static final DataTicket<Entity> SSC_ENTITY = DataTicket.create("ssc_entity", Entity.class);

	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;F)V", at = @At("HEAD"))
	private void captureEntity(T entity, S entityRenderState, float tickDelta, CallbackInfo ci) {
		entityRenderState.addGeckolibData(SSC_ENTITY, entity);
	}

	// 提交渲染时渲染覆盖层（茧），等价于旧版 render(Entity, ...) 入口
	@Inject(method = "submit", at = @At("HEAD"))
	private void renderOverlay(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
		Entity entity = state.getGeckolibData(SSC_ENTITY);
		if (entity != null) {
			float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
			// MultiBufferSource 参数在新管线中不再使用（EntityOverlayRenderSystem 内部走 SubmitNodeCollector），传 null
			EntityOverlayRenderSystem.render(entity, 0.0F, partialTick, poseStack, null, state.lightCoords);
		}
	}
}

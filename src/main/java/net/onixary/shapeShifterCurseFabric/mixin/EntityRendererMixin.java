package net.onixary.shapeShifterCurseFabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.onixary.shapeShifterCurseFabric.render.tech.EntityOverlayRenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.constant.dataticket.DataTicket;

@Environment(EnvType.CLIENT)
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {

	// 1.21.11 渲染管线改为 submit(RenderState)，RenderState 不再携带实体引用。
	// 用 GeckoLib 的 GeoRenderState DataTicket 把实体暂存到 RenderState，submit 时再取回。
	@Unique
	private static final DataTicket<Entity> SSC_ENTITY = DataTicket.create("ssc_entity", Entity.class);

	// 提取渲染状态时记录实体引用（供 submit 阶段渲染覆盖层使用）
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;F)V", at = @At("HEAD"))
	private void captureEntity(T entity, S entityRenderState, float tickDelta, CallbackInfo ci) {
		entityRenderState.addGeckolibData(SSC_ENTITY, entity);
	}

	// 提交渲染时渲染覆盖层（茧），等价于旧版 render(Entity, ...) 入口
	@Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At("HEAD"))
	private void renderOverlay(S entityRenderState, PoseStack matrices, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
		Entity entity = entityRenderState.getGeckolibData(SSC_ENTITY);
		if (entity != null) {
			float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
			// MultiBufferSource 参数在新管线中不再使用（EntityOverlayRenderSystem 内部走 SubmitNodeCollector），传 null
			EntityOverlayRenderSystem.render(entity, 0.0F, partialTick, matrices, null, entityRenderState.lightCoords);
		}
	}
}

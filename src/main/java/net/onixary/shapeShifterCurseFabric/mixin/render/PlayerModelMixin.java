package net.onixary.shapeShifterCurseFabric.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormRenderFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 1.21.11 修复形态 JSON hidden 部件失效：
 * LivingEntityRenderer.submit 中 vanilla 模型本体 submitModel 在 RenderLayer.submit（含 FormRenderFeature.submit
 * 里的 rM_PartA）之前就提交，且 setupAnim 会用 renderState.showXxx 覆盖 visible，导致 hidden 不生效。
 * 本 mixin 在每次 PlayerModel.setupAnim（vanilla 渲染管线固定调用点）末尾应用 FormModel.Hidden_X，
 * 使当帧 overlay 通道与下一帧 vanilla 主通道读到正确的 visible。
 */
@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {
	@Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"))
	private void ssc$applyFormHiddenParts(AvatarRenderState state, CallbackInfo ci) {
		Entity entity = Minecraft.getInstance().level.getEntity(state.id);
		if (!(entity instanceof AbstractClientPlayer player)) {
			return;
		}
		AvatarRenderer renderer = (AvatarRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
		FormRenderFeature.rM_PartA(renderer, player, (PlayerModel) (Object) this, new PoseStack(), 0);
	}
}

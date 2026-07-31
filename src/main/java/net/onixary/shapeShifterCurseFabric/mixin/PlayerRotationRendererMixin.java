package net.onixary.shapeShifterCurseFabric.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.additional_power.BatBlockAttachPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AvatarRenderer.class, priority = 100)
public class PlayerRotationRendererMixin {

	@Unique
	private boolean isAttachedToBlock(Player player) {
		return PowerHolderComponent.hasPower(player, BatBlockAttachPower.class, power ->
				power.isActive() && power.isAttached() && power.getAttachType() == BatBlockAttachPower.AttachType.SIDE
		);
	}

	@Unique
	private boolean isFirstPersonView() {
		Minecraft client = Minecraft.getInstance();
		return client.options.getCameraType() == CameraType.FIRST_PERSON;
	}

	// 1.21.11 AvatarRenderer 渲染管线重构：render() 已移除，
	// 旋转在 extractRenderState() 提取渲染状态时生效，故在提取前锁定实体旋转
	@Inject(
			method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
			at = @At("HEAD")
	)
	private void lockRotationWhenAttached(
			Avatar avatar,
			AvatarRenderState avatarRenderState,
			float tickDelta,
			CallbackInfo ci
	) {
		if (!(avatar instanceof AbstractClientPlayer player)) {
			return;
		}

		if (isFirstPersonView()) {
			return;
		}

		if (isAttachedToBlock(player)) {
			PowerHolderComponent.getPowers(player, BatBlockAttachPower.class).forEach(power -> {
				//player.setYaw(power.getTargetYaw());
				player.setYBodyRot(power.getTargetYaw());
				//player.prevYaw = power.getTargetYaw();
				player.yBodyRotO = power.getTargetYaw();
			});
		}
	}
}

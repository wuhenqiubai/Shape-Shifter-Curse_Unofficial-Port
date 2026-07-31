package net.onixary.shapeShifterCurseFabric.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.onixary.shapeShifterCurseFabric.additional_power.DisablePlayerRotationPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = AvatarRenderer.class, priority = 100)
public class DisablePlayerRotationRendererMixin {

	// 1.21.11 AvatarRenderer 渲染管线重构：render() 已移除，
	// 旋转在 extractRenderState() 提取渲染状态时生效，故在提取前锁定实体旋转
	@Inject(
			method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
			at = @At("HEAD")
	)
	private void lockRotationToNorth(
			Avatar avatar,
			AvatarRenderState avatarRenderState,
			float tickDelta,
			CallbackInfo ci
	) {
		if (!(avatar instanceof AbstractClientPlayer player)) {
			return;
		}

		if (Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON) {
			return;
		}

		if (PowerHolderComponent.hasPower(player, DisablePlayerRotationPower.class)) {
			// Lock body and head facing north (yaw = 180 in Minecraft coordinate system)
			player.yBodyRotO = 180.0F;
			player.yBodyRot = 180.0F;
			player.yHeadRotO = 180.0F;
			player.yHeadRot = 180.0F;
		}
	}
}

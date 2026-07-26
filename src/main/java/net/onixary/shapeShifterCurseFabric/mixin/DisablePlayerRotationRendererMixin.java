package net.onixary.shapeShifterCurseFabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.onixary.shapeShifterCurseFabric.additional_power.DisablePlayerRotationPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = PlayerRenderer.class, priority = 100)
public class DisablePlayerRotationRendererMixin {

    @Inject(
            method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD")
    )
    private void lockRotationToNorth(
            AbstractClientPlayer player,
            float yaw,
            float tickDelta,
            PoseStack matrices,
            MultiBufferSource vertexConsumers,
            int light,
            CallbackInfo ci
    ) {
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

package net.onixary.shapeShifterCurseFabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.additional_power.BatBlockAttachPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerRenderer.class, priority = 100)
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

    @Inject(
            method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD")
    )
    private void lockRotationWhenAttached(
            AbstractClientPlayer player,
            float yaw,
            float tickDelta,
            PoseStack matrices,
            MultiBufferSource vertexConsumers,
            int light,
            CallbackInfo ci
    ) {
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

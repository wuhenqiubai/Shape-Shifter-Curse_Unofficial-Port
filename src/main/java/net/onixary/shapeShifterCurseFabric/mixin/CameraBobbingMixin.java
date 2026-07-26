package net.onixary.shapeShifterCurseFabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.additional_power.FormCameraBobbingPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 拦截 GameRenderer.bobView，根据玩家身上的 FormCameraBobbingPower
 * 将原版视角晃动替换为自定义晃动逻辑。
 * <p>
 * 若玩家没有该 Power（或条件不满足），则原版晃动照常运行。
 */
@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class CameraBobbingMixin {

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void shape_shifter_curse$customBobView(PoseStack matrices, float tickDelta, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (!(client.getCameraEntity() instanceof Player player)) return;

        List<FormCameraBobbingPower> powers = PowerHolderComponent.getPowers(player, FormCameraBobbingPower.class);

        FormCameraBobbingPower activePower = null;
        for (FormCameraBobbingPower p : powers) {
            if (p.isActive()) {
                activePower = p;
                break;
            }
        }
        if (activePower == null) return;

        // 阻止原版晃动，由下方自定义逻辑接管
        ci.cancel();
        shape_shifter_curse$applyBobbing(matrices, tickDelta, player, activePower.bobbingType);
    }

    /**
     * 根据 bobbingType 分派对应的晃动逻辑。
     */
    @Unique
    private void shape_shifter_curse$applyBobbing(PoseStack matrices, float tickDelta, Player player, String bobbingType) {
        switch (bobbingType) {
            case "none"   -> { /* 完全无晃动，不做任何矩阵变换 */ }
            case "float"  -> shape_shifter_curse$applyFloatBobbing(matrices, tickDelta, player);
            case "feral" -> shape_shifter_curse$applyFeralBobbing(matrices, tickDelta, player);
            case "bat"    -> shape_shifter_curse$applyBatBobbing(matrices, tickDelta, player);
            default       -> shape_shifter_curse$applyDefaultBobbing(matrices, tickDelta, player);
        }
    }

    // -------------------------------------------------------------------------
    // 各 BobbingType 实现
    // -------------------------------------------------------------------------

    /**
     * default — 复现原版晃动逻辑（用于不识别的 bobbingType 时兜底）。
     */
    @Unique
    private void shape_shifter_curse$applyDefaultBobbing(PoseStack matrices, float tickDelta, Player player) {
        float deltaH   = player.walkDist - player.walkDistO;
        float phase    = -(player.walkDist + deltaH * tickDelta);
        float amplitude = Mth.lerp(tickDelta, player.oBob, player.bob);

        matrices.translate(
                (double)(Mth.sin(phase * Mth.PI) * amplitude * 0.5f),
                (double)(-Math.abs(Mth.cos(phase * Mth.PI) * amplitude)),
                0.0
        );
        matrices.mulPose(Axis.ZP.rotationDegrees(
                Mth.sin(phase * Mth.PI) * amplitude * 3.0f));
        matrices.mulPose(Axis.XP.rotationDegrees(
                Math.abs(Mth.cos(phase * Mth.PI - 0.2f) * amplitude) * 5.0f));
    }

    /**
     * float — 漂浮，慢速上下移动。
     */
    @Unique
    private void shape_shifter_curse$applyFloatBobbing(PoseStack matrices, float tickDelta, Player player) {
        float deltaH    = player.walkDist - player.walkDistO;
        float phase     = -(player.walkDist + deltaH * tickDelta);
        float amplitude = Mth.lerp(tickDelta, player.oBob, player.bob);
        float sin       = Mth.sin(phase * Mth.PI * 0.6f);

        matrices.translate(
                0.0,
                (double)(-Math.abs(sin) * amplitude) * 0.75f,
                0.0
        );
    }

    /**
     * feral
     */
    @Unique
    private void shape_shifter_curse$applyFeralBobbing(PoseStack matrices, float tickDelta, Player player) {
        float deltaH    = player.walkDist - player.walkDistO;
        float phase     = -(player.walkDist + deltaH * tickDelta);
        float amplitude = Mth.lerp(tickDelta, player.oBob, player.bob) * 0.55f;

        matrices.translate(
                (double)(Mth.sin(phase * Mth.PI) * amplitude * 0.3f),
                (double)(-Math.abs(Mth.cos(phase * Mth.PI * 1.1f) * amplitude) * 1.2f),
                0.0
        );
        matrices.mulPose(Axis.ZP.rotationDegrees(
                Mth.sin(phase * Mth.PI) * amplitude * 2.0f));
        matrices.mulPose(Axis.XP.rotationDegrees(
                Math.abs(Mth.cos(phase * Mth.PI - 0.2f) * amplitude) * 3.0f));
    }

    /**
     * bat
     */
    @Unique
    private void shape_shifter_curse$applyBatBobbing(PoseStack matrices, float tickDelta, Player player) {
        float deltaH    = player.walkDist - player.walkDistO;
        float phase     = -(player.walkDist + deltaH * tickDelta);
        float amplitude = Mth.lerp(tickDelta, player.oBob, player.bob);
        float sin       = Mth.sin(phase * Mth.PI );

        matrices.translate(
                0.0,
                (double)(-Math.abs(sin) * amplitude * 0.8f),
                0.0
        );
        matrices.mulPose(Axis.XP.rotationDegrees(
                sin * amplitude * 2.0f));
        matrices.mulPose(Axis.ZP.rotationDegrees(
                sin * amplitude * 1.0f));
    }
}

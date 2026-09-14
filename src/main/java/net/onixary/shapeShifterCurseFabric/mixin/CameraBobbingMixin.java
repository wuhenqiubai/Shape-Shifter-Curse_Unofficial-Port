package net.onixary.shapeShifterCurseFabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
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
 * <p>
 * 26.1 迁移说明：{@code bobView} 的形参只剩 {@code (CameraRenderState, PoseStack)} ——
 * 原来那个 {@code tickDelta} 没有了，因为原版已把**插值后的**晃动参数预先抽进 render state：
 * <pre>
 *   cameraState.entityRenderState.backwardsInterpolatedWalkDistance
 *   cameraState.entityRenderState.bob
 * </pre>
 * 所以本类的各个 apply*Bobbing 不再自己用 {@code avatarState().getXxx(tickDelta)} 重算，
 * 改为直接接收这两个已插值好的值（phase / amplitude）。
 */
@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class CameraBobbingMixin {

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void shape_shifter_curse$customBobView(CameraRenderState cameraState, PoseStack poseStack, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (!(client.getCameraEntity() instanceof Player player)) return;
        // 与原版 bobView 同样的前置条件：渲染状态必须描述的是玩家
        if (!cameraState.entityRenderState.isPlayer) return;

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

        // 保留 SSC 原有的取负（与 avatarState 版的符号一致）
        float phase = -cameraState.entityRenderState.backwardsInterpolatedWalkDistance;
        float amplitude = cameraState.entityRenderState.bob;
        shape_shifter_curse$applyBobbing(poseStack, phase, amplitude, activePower.bobbingType);
    }

    /**
     * 根据 bobbingType 分派对应的晃动逻辑。
     */
    @Unique
    private void shape_shifter_curse$applyBobbing(PoseStack matrices, float phase, float amplitude, String bobbingType) {
        switch (bobbingType) {
            case "none"   -> { /* 完全无晃动，不做任何矩阵变换 */ }
            case "float"  -> shape_shifter_curse$applyFloatBobbing(matrices, phase, amplitude);
            case "feral" -> shape_shifter_curse$applyFeralBobbing(matrices, phase, amplitude);
            case "bat"    -> shape_shifter_curse$applyBatBobbing(matrices, phase, amplitude);
            default       -> shape_shifter_curse$applyDefaultBobbing(matrices, phase, amplitude);
        }
    }

    // -------------------------------------------------------------------------
    // 各 BobbingType 实现（数学与原实现逐字一致，只是 phase/amplitude 改为入参）
    // -------------------------------------------------------------------------

    /**
     * default — 复现原版晃动逻辑（用于不识别的 bobbingType 时兜底）。
     */
    @Unique
    private void shape_shifter_curse$applyDefaultBobbing(PoseStack matrices, float phase, float amplitude) {
        matrices.translate(
                Mth.sin(phase * Mth.PI) * amplitude * 0.5F,
                -Math.abs(Mth.cos(phase * Mth.PI) * amplitude),
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
    private void shape_shifter_curse$applyFloatBobbing(PoseStack matrices, float phase, float amplitude) {
        float sin = Mth.sin(phase * Mth.PI * 0.6f);

        matrices.translate(
                0.0,
                -Math.abs(sin) * amplitude * 0.75f,
                0.0
        );
    }

    /**
     * feral
     */
    @Unique
    private void shape_shifter_curse$applyFeralBobbing(PoseStack matrices, float phase, float amplitude) {
        float amp = amplitude * 0.55f;

        matrices.translate(
                Mth.sin(phase * Mth.PI) * amp * 0.3f,
                -Math.abs(Mth.cos(phase * Mth.PI * 1.1f) * amp) * 1.2f,
                0.0
        );
        matrices.mulPose(Axis.ZP.rotationDegrees(
                Mth.sin(phase * Mth.PI) * amp * 2.0f));
        matrices.mulPose(Axis.XP.rotationDegrees(
                Math.abs(Mth.cos(phase * Mth.PI - 0.2f) * amp) * 3.0f));
    }

    /**
     * bat
     */
    @Unique
    private void shape_shifter_curse$applyBatBobbing(PoseStack matrices, float phase, float amplitude) {
        float sin = Mth.sin(phase * Mth.PI);

        matrices.translate(
                0.0,
                -Math.abs(sin) * amplitude * 0.8f,
                0.0
        );
        matrices.mulPose(Axis.XP.rotationDegrees(
                sin * amplitude * 2.0f));
        matrices.mulPose(Axis.ZP.rotationDegrees(
                sin * amplitude * 1.0f));
    }
}

package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.onixary.shapeShifterCurseFabric.additional_power.DisableHurtCameraPower;
import net.onixary.shapeShifterCurseFabric.screen_effect.TransformOverlayRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class GameRendererMixin {
    // ⚠ 26.1: bobHurt 的签名与实现都变了 —— 原为 bobHurt(PoseStack, float)，受伤方向取自
    //   LivingEntity.getHurtDir()；26.1 改为 bobHurt(CameraRenderState, PoseStack)，方向直接读
    //   cameraState.entityRenderState.hurtDir 字段，方法体内已无 getHurtDir() 调用。
    //   照旧写会 "Scanned 0 target(s)" 注入失败（required=true → 加载 GameRenderer 即崩，客户端起不来）。
    //   改注入 Mth.sin 调用处：死亡倾斜（Z 轴 40°）在其之前已执行、受伤旋转在其之后，
    //   语义与原注点（"死亡倾斜之后、受伤旋转之前"）等价。Mth.sin 在 bobHurt 内只此一处。
    @Inject(method = "bobHurt", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/Mth;sin(D)F"), cancellable = true)
    private void shape_shifter_curse$disableHurtCamera(CameraRenderState cameraState, PoseStack poseStack, CallbackInfo ci) {
        if (PowerHolderComponent.hasPower(Minecraft.getInstance().getCameraEntity(), DisableHurtCameraPower.class)) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", ordinal = 0))
    private void shape_shifter_curse$renderOverlayAboveHud(net.minecraft.client.DeltaTracker tickCounter, boolean tick, CallbackInfo ci) {
        TransformOverlayRenderer.render();
    }

    /** Guard getNightVisionStrength against null entity (Fabric Loader 0.19.3 mapping corruption). */
    @ModifyExpressionValue(
            method = "getNightVisionScale(Lnet/minecraft/world/entity/LivingEntity;F)F",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getEffect(Lnet/minecraft/core/Holder;)Lnet/minecraft/world/effect/MobEffectInstance;"))
    private static MobEffectInstance ssc$guardNightVision(MobEffectInstance raw) {
        if (raw == null) return new MobEffectInstance(MobEffects.NIGHT_VISION, Integer.MAX_VALUE, 0);
        return raw;
    }
}
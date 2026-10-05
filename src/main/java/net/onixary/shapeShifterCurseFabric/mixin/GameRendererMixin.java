package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
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
    // This point is after vanilla's death tilt and before the hurt camera rotations.
    @Inject(method = "bobHurt", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getHurtDir()F"), cancellable = true)
    private void shape_shifter_curse$disableHurtCamera(PoseStack poseStack, float f, CallbackInfo ci) {
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
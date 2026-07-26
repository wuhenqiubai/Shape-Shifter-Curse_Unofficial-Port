package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.onixary.shapeShifterCurseFabric.screen_effect.TransformOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", ordinal = 0))
    private void shape_shifter_curse$renderOverlayAboveHud(net.minecraft.client.DeltaTracker tickCounter, boolean tick, CallbackInfo ci) {
        TransformOverlay.INSTANCE.render();
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
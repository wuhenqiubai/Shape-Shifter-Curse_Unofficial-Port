package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.onixary.shapeShifterCurseFabric.screen_effect.TransformOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V", ordinal = 0))
    private void shape_shifter_curse$renderOverlayAboveHud(net.minecraft.client.render.RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        TransformOverlay.INSTANCE.render();
    }

    /** Guard getNightVisionStrength against null entity (Fabric Loader 0.19.3 mapping corruption). */
    @ModifyExpressionValue(
            method = "getNightVisionStrength(Lnet/minecraft/entity/LivingEntity;F)F",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;getStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Lnet/minecraft/entity/effect/StatusEffectInstance;"))
    private static StatusEffectInstance ssc$guardNightVision(StatusEffectInstance raw) {
        if (raw == null) return new StatusEffectInstance(StatusEffects.NIGHT_VISION, Integer.MAX_VALUE, 0);
        return raw;
    }
}
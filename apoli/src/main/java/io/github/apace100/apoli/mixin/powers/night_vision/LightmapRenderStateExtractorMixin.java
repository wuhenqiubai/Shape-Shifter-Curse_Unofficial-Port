package io.github.apace100.apoli.mixin.powers.night_vision;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.NightVisionPower;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightmapRenderStateExtractor.class)
public abstract class LightmapRenderStateExtractorMixin {
    @Definition(id = "hasEffect", method = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/core/Holder;)Z")
    @Definition(id = "NIGHT_VISION", field = "Lnet/minecraft/world/effect/MobEffects;NIGHT_VISION:Lnet/minecraft/core/Holder;")
    @Expression("?.hasEffect(NIGHT_VISION)")
    @WrapOperation(method = "extract", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean checkHasNightVisionPower(LocalPlayer instance, Holder<MobEffect> holder, Operation<Boolean> original) {
        return original.call(instance, holder) || PowerHolderComponent.KEY.get(instance).getPowers(NightVisionPower.class).stream().anyMatch(NightVisionPower::isActive);
    }
}

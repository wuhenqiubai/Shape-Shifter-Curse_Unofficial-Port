package io.github.apace100.apoli.mixin.legacy.core.level_render;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.ApoliClient;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.EntityGlowPower;
import io.github.apace100.apoli.power.SelfGlowPower;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelExtractor.class)
public abstract class LevelExtractorMixin {
    @Shadow public abstract void allChanged();
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "extract", at = @At("HEAD"))
    private void updateChunksIfRenderChanged(CallbackInfo ci) {
        if(ApoliClient.shouldReloadWorldRenderer) {
            allChanged();
            ApoliClient.shouldReloadWorldRenderer = false;
        }
    }

    @ModifyReturnValue(method = "extractEntity", at = @At("RETURN"))
    private EntityRenderState setColors(EntityRenderState renderState, @Local(argsOnly = true) Entity entity) {
        for (EntityGlowPower power : PowerHolderComponent.getPowers(this.minecraft.getCameraEntity(), EntityGlowPower.class)) {
            if (power.doesApply(entity)) {
                if (!power.usesTeams()) {
                    renderState.outlineColor = ARGB.colorFromFloat(1f, power.getRed(), power.getGreen(), power.getBlue());
                }
            }
        }

        for (SelfGlowPower power : PowerHolderComponent.getPowers(entity, SelfGlowPower.class)) {
            if (!power.usesTeams()) {
                renderState.outlineColor = ARGB.colorFromFloat(1f, power.getRed(), power.getGreen(), power.getBlue());
            }
        }

        return renderState;
    }
}

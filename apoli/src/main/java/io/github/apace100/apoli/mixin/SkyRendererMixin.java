package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PhasingPower;
import io.github.apace100.apoli.util.MiscUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
    @Inject(method = "renderSkyDisc", at = @At("HEAD"), cancellable = true)
    private void skipSkyRenderingForPhasingBlindness(int skyColor, CallbackInfo ci) {
        if (Minecraft.getInstance().getCameraEntity() instanceof LivingEntity cameraEntity) {
            List<PhasingPower> phasings = PowerHolderComponent.getPowers(cameraEntity, PhasingPower.class);
            if (phasings.stream().anyMatch(pp -> pp.getRenderType() == PhasingPower.RenderType.BLINDNESS)) {
                if (MiscUtil.getInWallBlockState(cameraEntity) != null) {
                    ci.cancel();
                }
            }
        }
    }
}

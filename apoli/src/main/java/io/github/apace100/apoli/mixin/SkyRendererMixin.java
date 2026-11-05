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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

// 详见 docs/connector-neoforge-compat.md
@Mixin(SkyRenderer.class)
@Environment(EnvType.CLIENT)
public class SkyRendererMixin {

    @Unique
    private static boolean apoli$skipSkyForPhasingBlindness() {
        if(!(Minecraft.getInstance().getCameraEntity() instanceof LivingEntity livingEntity)) {
            return false;
        }
        List<PhasingPower> phasings = PowerHolderComponent.getPowers(livingEntity, PhasingPower.class);
        return phasings.stream().anyMatch(pp -> pp.getRenderType() == PhasingPower.RenderType.BLINDNESS)
                && MiscUtil.getInWallBlockState(livingEntity) != null;
    }

    @Inject(method = "renderSkyDisc", at = @At("HEAD"), cancellable = true)
    private void apoli$skipSkyDisc(CallbackInfo ci) {
        if(apoli$skipSkyForPhasingBlindness()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderSunriseAndSunset", at = @At("HEAD"), cancellable = true)
    private void apoli$skipSunriseAndSunset(CallbackInfo ci) {
        if(apoli$skipSkyForPhasingBlindness()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderSunMoonAndStars", at = @At("HEAD"), cancellable = true)
    private void apoli$skipSunMoonAndStars(CallbackInfo ci) {
        if(apoli$skipSkyForPhasingBlindness()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderDarkDisc", at = @At("HEAD"), cancellable = true)
    private void apoli$skipDarkDisc(CallbackInfo ci) {
        if(apoli$skipSkyForPhasingBlindness()) {
            ci.cancel();
        }
    }
}

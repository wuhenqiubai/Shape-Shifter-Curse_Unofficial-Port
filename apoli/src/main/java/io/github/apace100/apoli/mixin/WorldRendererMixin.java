package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import io.github.apace100.apoli.ApoliClient;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.EntityGlowPower;
import io.github.apace100.apoli.power.PhasingPower;
import io.github.apace100.apoli.power.SelfGlowPower;
import io.github.apace100.apoli.util.MiscUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(LevelRenderer.class)
public abstract class WorldRendererMixin {

    @Final
    @Shadow
    private Minecraft minecraft;

    @Shadow public abstract void allChanged();

    @Inject(method = "lambda$addSkyPass$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSkyDisc(I)V"), cancellable = true)
    private static void skipSkyRenderingForPhasingBlindness(GpuBufferSlice gpuBufferSlice, SkyRenderState skyRenderState, SkyRenderer skyRenderer, CallbackInfo ci) {
        if(Minecraft.getInstance().getCameraEntity() instanceof LivingEntity) {
            List<PhasingPower> phasings = PowerHolderComponent.getPowers(Minecraft.getInstance().getCameraEntity(), PhasingPower.class);
            if(phasings.stream().anyMatch(pp -> pp.getRenderType() == PhasingPower.RenderType.BLINDNESS)) {
                if(MiscUtil.getInWallBlockState((LivingEntity)Minecraft.getInstance().getCameraEntity()) != null) {
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void updateChunksIfRenderChanged(CallbackInfo ci) {
        if(ApoliClient.shouldReloadWorldRenderer) {
            allChanged();
            ApoliClient.shouldReloadWorldRenderer = false;
        }
    }

    @ModifyReturnValue(method = "extractEntity", at = @At("RETURN"))
    private EntityRenderState setColors(EntityRenderState renderState, @Local(argsOnly = true) Entity entity) {
        for (EntityGlowPower power : PowerHolderComponent.getPowers(minecraft.getCameraEntity(), EntityGlowPower.class)) {
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

package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import io.github.apace100.apoli.ApoliClient;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.EntityGlowPower;
import io.github.apace100.apoli.power.SelfGlowPower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LevelRenderer.class)
public abstract class WorldRendererMixin {

    @Final
    @Shadow
    private Minecraft minecraft;

    @Shadow public abstract void allChanged();

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void updateChunksIfRenderChanged(GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, Matrix4f frustumMatrix, Matrix4f projectionMatrix, Matrix4f cullingProjectionMatrix, GpuBufferSlice shaderFog, Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
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

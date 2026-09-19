package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.ApoliClient;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.EntityGlowPower;
import io.github.apace100.apoli.power.PhasingPower;
import io.github.apace100.apoli.power.SelfGlowPower;
import io.github.apace100.apoli.util.MiscUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(LevelRenderer.class)
public abstract class WorldRendererMixin {

    @Final
    @Shadow
    private Minecraft minecraft;

    @Unique
    private Entity renderEntity;

    @Shadow public abstract void allChanged();

    @Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Ljava/lang/Runnable;run()V", shift = At.Shift.AFTER, ordinal = 0), cancellable = true)
    private void skipSkyRenderingForPhasingBlindness(Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick, Camera camera, boolean isFoggy, Runnable skyFogSetup, CallbackInfo ci) {
        if(camera.getEntity() instanceof LivingEntity) {
            List<PhasingPower> phasings = PowerHolderComponent.getPowers(camera.getEntity(), PhasingPower.class);
            if(phasings.stream().anyMatch(pp -> pp.getRenderType() == PhasingPower.RenderType.BLINDNESS)) {
                if(MiscUtil.getInWallBlockState((LivingEntity)camera.getEntity()) != null) {
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void updateChunksIfRenderChanged(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        if(ApoliClient.shouldReloadWorldRenderer) {
            allChanged();
            ApoliClient.shouldReloadWorldRenderer = false;
        }
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/Entity;getTeamColor()I"))
    private void getEntity(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci, @Local Entity entity) {
        this.renderEntity = entity;
    }

    @ModifyArgs(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OutlineBufferSource;setColor(IIII)V"))
    private void setColors(Args args) {
        for (EntityGlowPower power : PowerHolderComponent.getPowers(minecraft.getCameraEntity(), EntityGlowPower.class)) {
            if (power.doesApply(renderEntity)) {
                if (!power.usesTeams()) {
                    args.set(0, (int)(power.getRed() * 255.0F));
                    args.set(1, (int)(power.getGreen() * 255.0F));
                    args.set(2, (int)(power.getBlue() * 255.0F));
                }
            }
        }
        for (SelfGlowPower power : PowerHolderComponent.getPowers(renderEntity, SelfGlowPower.class)) {
            if (!power.usesTeams()) {
                args.set(0, (int)(power.getRed() * 255.0F));
                args.set(1, (int)(power.getGreen() * 255.0F));
                args.set(2, (int)(power.getBlue() * 255.0F));
            }
        }
    }
}
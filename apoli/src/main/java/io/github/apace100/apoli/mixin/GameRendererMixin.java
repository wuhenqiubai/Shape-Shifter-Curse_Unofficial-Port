package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    @Final
    private Camera mainCamera;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private boolean effectActive;

    @Shadow protected abstract void setPostEffect(Identifier postEffectId);

    @Shadow public abstract void clearPostEffect();

    @Unique
    private Identifier currentlyLoadedShader;

    @Inject(at = @At("TAIL"), method = "checkEntityPostEffect")
    private void loadShaderFromPowerOnCameraEntity(Entity entity, CallbackInfo ci) {
        PowerHolderComponent.withPower(minecraft.getCameraEntity(), ShaderPower.class, null, shaderPower -> {
            Identifier shaderLoc = shaderPower.getShaderLocation();
            if(this.minecraft.getResourceManager().getResource(shaderLoc).isPresent()) {
                this.setPostEffect(shaderLoc);
                currentlyLoadedShader = shaderLoc;
            }
        });
    }

    @Inject(at = @At("HEAD"), method = "render")
    private void loadShaderFromPower(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        PowerHolderComponent.withPower(minecraft.getCameraEntity(), ShaderPower.class, null, shaderPower -> {
            Identifier shaderLoc = shaderPower.getShaderLocation();
            if(currentlyLoadedShader != shaderLoc) {
                if(this.minecraft.getResourceManager().getResource(shaderLoc).isPresent()) {
                    this.setPostEffect(shaderLoc);
                    currentlyLoadedShader = shaderLoc;
                }
            }
        });
        if(!PowerHolderComponent.hasPower(minecraft.getCameraEntity(), ShaderPower.class) && currentlyLoadedShader != null) {
            this.clearPostEffect();
            this.effectActive = false;
            currentlyLoadedShader = null;
        }
    }

    @Inject(method = "extractGui", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", shift = At.Shift.AFTER))
    private void renderOverlayPowers(DeltaTracker deltaTracker, boolean shouldRenderLevel, boolean resourcesLoaded, CallbackInfo ci, @Local(name = "graphics") GuiGraphicsExtractor graphics) {
        boolean hudHidden = this.minecraft.options.hideGui;
        boolean thirdPerson = !minecraft.options.getCameraType().isFirstPerson();
        PowerHolderComponent.withPower(minecraft.getCameraEntity(), OverlayPower.class, p -> {
            if(p.getDrawPhase() != OverlayPower.DrawPhase.ABOVE_HUD) {
                return false;
            }
            if(hudHidden && p.doesHideWithHud()) {
                return false;
            }
            if(thirdPerson && !p.shouldBeVisibleInThirdPerson()) {
                return false;
            }
            return true;
        }, p -> p.render(graphics));
    }

    @Inject(at = @At("HEAD"), method = "togglePostEffect", cancellable = true)
    private void disableShaderToggle(CallbackInfo ci) {
        PowerHolderComponent.withPower(minecraft.getCameraEntity(), ShaderPower.class, null, shaderPower -> {
            Identifier shaderLoc = shaderPower.getShaderLocation();
            if(!shaderPower.isToggleable() && currentlyLoadedShader == shaderLoc) {
                ci.cancel();
            }
        });
    }

    // NightVisionPower
    @Inject(at = @At("HEAD"), method = "getNightVisionScale", cancellable = true)
    private static void getNightVisionStrength(LivingEntity livingEntity, float f, CallbackInfoReturnable<Float> info) {
        if (livingEntity instanceof Player && !livingEntity.hasEffect(MobEffects.NIGHT_VISION)) {
            List<NightVisionPower> nvs = PowerHolderComponent.KEY.get(livingEntity).getPowers(NightVisionPower.class);
            Optional<Float> strength = nvs.stream().filter(NightVisionPower::isActive).map(NightVisionPower::getStrength).max(Float::compareTo);
            strength.ifPresent(info::setReturnValue);
        }
    }

    @WrapOperation(method = "extractCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getFluidInCamera()Lnet/minecraft/world/level/material/FogType;"))
    private FogType modifySubmersionType(Camera camera, Operation<FogType> original) {
        FogType fogType = original.call(camera);
        if(camera.entity() instanceof LivingEntity) {
            for(ModifyCameraSubmersionTypePower p : PowerHolderComponent.getPowers(camera.entity(), ModifyCameraSubmersionTypePower.class)) {
                if(p.doesModify(fogType)) {
                    return p.getNewType();
                }
            }
        }
        return fogType;
    }

    private HashMap<BlockPos, BlockState> savedStates = new HashMap<>();

    // PHASING: remove_blocks
    @Inject(at = @At(value = "HEAD"), method = "render")
    private void beforeRender(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        List<PhasingPower> phasings = PowerHolderComponent.getPowers(mainCamera.entity(), PhasingPower.class);
        if (phasings.stream().anyMatch(pp -> pp.getRenderType() == PhasingPower.RenderType.REMOVE_BLOCKS)) {
            float view = phasings.stream().filter(pp -> pp.getRenderType() == PhasingPower.RenderType.REMOVE_BLOCKS).map(PhasingPower::getViewDistance).min(Float::compareTo).get();
            Set<BlockPos> eyePositions = getEyePos(0.25F, 0.05F, 0.25F);
            Set<BlockPos> noLongerEyePositions = new HashSet<>();
            for (BlockPos p : savedStates.keySet()) {
                if (!eyePositions.contains(p)) {
                    noLongerEyePositions.add(p);
                }
            }
            for (BlockPos eyePosition : noLongerEyePositions) {
                BlockState state = savedStates.get(eyePosition);
                minecraft.level.setBlockAndUpdate(eyePosition, state);
                savedStates.remove(eyePosition);
            }
            for (BlockPos p : eyePositions) {
                BlockState stateAtP = minecraft.level.getBlockState(p);
                if (!savedStates.containsKey(p) && !minecraft.level.isEmptyBlock(p) && !(stateAtP.getBlock() instanceof LiquidBlock)) {
                    savedStates.put(p, stateAtP);
                    minecraft.level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
                }
            }
        } else if (savedStates.size() > 0) {
            Set<BlockPos> noLongerEyePositions = new HashSet<>(savedStates.keySet());
            for (BlockPos eyePosition : noLongerEyePositions) {
                BlockState state = savedStates.get(eyePosition);
                minecraft.level.setBlockAndUpdate(eyePosition, state);
                savedStates.remove(eyePosition);
            }
        }
    }

    // PHASING
    private Set<BlockPos> getEyePos(float rangeX, float rangeY, float rangeZ) {
        Vec3 pos = mainCamera.entity().position().add(0, mainCamera.entity().getEyeHeight(mainCamera.entity().getPose()), 0);
        AABB cameraBox = new AABB(pos, pos);
        cameraBox = cameraBox.inflate(rangeX, rangeY, rangeZ);
        HashSet<BlockPos> set = new HashSet<>();
        BlockPos.betweenClosedStream(cameraBox).forEach(p -> set.add(p.immutable()));
        return set;
    }

    // NIGHT VISION
    @ModifyReturnValue(method = "getNightVisionScale", at = @At("RETURN"))
    private static float adjustNightVisionScale(float value, @Local(argsOnly = true, name = "camera") LivingEntity camera) {
        Optional<Float> nightVisionStrength = PowerHolderComponent.KEY.get(camera).getPowers(NightVisionPower.class).stream().filter(NightVisionPower::isActive).map(NightVisionPower::getStrength).max(Float::compareTo);
        return nightVisionStrength.map(aFloat -> Math.max(aFloat, value)).orElse(value);
    }
}

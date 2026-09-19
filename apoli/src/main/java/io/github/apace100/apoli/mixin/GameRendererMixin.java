package io.github.apace100.apoli.mixin;

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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
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
    @Shadow @Final private ResourceManager resourceManager;

    @Shadow protected abstract void loadEffect(ResourceLocation resourceLocation);

    @Shadow @Nullable private PostChain postEffect;
    @Unique
    private ResourceLocation currentlyLoadedShader;

    @Inject(at = @At("TAIL"), method = "checkEntityPostEffect")
    private void loadShaderFromPowerOnCameraEntity(Entity entity, CallbackInfo ci) {
        PowerHolderComponent.withPower(minecraft.getCameraEntity(), ShaderPower.class, null, shaderPower -> {
            ResourceLocation shaderLoc = shaderPower.getShaderLocation();
            if(this.resourceManager.getResource(shaderLoc).isPresent()) {
                this.loadEffect(shaderLoc);
                currentlyLoadedShader = shaderLoc;
            }
        });
    }

    @Inject(at = @At("HEAD"), method = "render")
    private void loadShaderFromPower(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        PowerHolderComponent.withPower(minecraft.getCameraEntity(), ShaderPower.class, null, shaderPower -> {
            ResourceLocation shaderLoc = shaderPower.getShaderLocation();
            if(currentlyLoadedShader != shaderLoc) {
                if(this.resourceManager.getResource(shaderLoc).isPresent()) {
                    this.loadEffect(shaderLoc);
                    currentlyLoadedShader = shaderLoc;
                }
            }
        });
        if(!PowerHolderComponent.hasPower(minecraft.getCameraEntity(), ShaderPower.class) && currentlyLoadedShader != null) {
            if(this.postEffect != null) {
                this.postEffect.close();
                this.postEffect = null;
            }
            this.effectActive = false;
            currentlyLoadedShader = null;
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", shift = At.Shift.AFTER))
    private void renderOverlayPowers(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci, @Local GuiGraphics guiGraphics) {
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
        }, p -> p.render());
    }

    @Inject(at = @At("HEAD"), method = "togglePostEffect", cancellable = true)
    private void disableShaderToggle(CallbackInfo ci) {
        PowerHolderComponent.withPower(minecraft.getCameraEntity(), ShaderPower.class, null, shaderPower -> {
            ResourceLocation shaderLoc = shaderPower.getShaderLocation();
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

    @WrapOperation(method = "getFov", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getFluidInCamera()Lnet/minecraft/world/level/material/FogType;"))
    private FogType modifySubmersionType(Camera camera, Operation<FogType> original) {
        FogType fogType = original.call(camera);
        if(camera.getEntity() instanceof LivingEntity) {
            for(ModifyCameraSubmersionTypePower p : PowerHolderComponent.getPowers(camera.getEntity(), ModifyCameraSubmersionTypePower.class)) {
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
        List<PhasingPower> phasings = PowerHolderComponent.getPowers(mainCamera.getEntity(), PhasingPower.class);
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
    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V"), method = "renderLevel")
    private void preventThirdPerson(Camera camera, BlockGetter area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, Operation<Float> original) {
        if (PowerHolderComponent.getPowers(camera.getEntity(), PhasingPower.class).stream().anyMatch(pp -> pp.getRenderType() == PhasingPower.RenderType.REMOVE_BLOCKS)) {
            camera.setup(area, focusedEntity, false, false, tickDelta);
        } else {
            original.call(camera, area, focusedEntity, thirdPerson, inverseView, tickDelta);
        }
    }

    private Set<BlockPos> getEyePos(float rangeX, float rangeY, float rangeZ) {
        Vec3 pos = mainCamera.getEntity().position().add(0, mainCamera.getEntity().getEyeHeight(mainCamera.getEntity().getPose()), 0);
        AABB cameraBox = new AABB(pos, pos);
        cameraBox = cameraBox.inflate(rangeX, rangeY, rangeZ);
        HashSet<BlockPos> set = new HashSet<>();
        BlockPos.betweenClosedStream(cameraBox).forEach(p -> set.add(p.immutable()));
        return set;
    }
}

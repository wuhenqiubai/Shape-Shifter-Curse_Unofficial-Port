package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.InvisibilityPower;
import io.github.apace100.apoli.power.ModelColorPower;
import io.github.apace100.apoli.power.PreventFeatureRenderPower;
import io.github.apace100.apoli.power.ShakingPower;
import io.github.apace100.apoli.util.ApoliLivingEntityRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<S>> extends EntityRenderer<T, S> {

    @Shadow public abstract Identifier getTextureLocation(S renderState);

    protected LivingEntityRendererMixin(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Inject(method = "isShaking", at = @At("HEAD"), cancellable = true)
    private void letPlayersShakeTheirBodies(S renderState, CallbackInfoReturnable<Boolean> cir) {
        if(PowerHolderComponent.hasPower(renderState, ShakingPower.class)) {
            cir.setReturnValue(true);
        }
    }

    @ModifyExpressionValue(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;appearsGlowing()Z"))
    private boolean preventOutlineRendering(boolean original, @Local(argsOnly = true) LivingEntityRenderState renderState) {
        List<InvisibilityPower> invisibilityPowers = PowerHolderComponent.getPowers(renderState, InvisibilityPower.class);
        if(invisibilityPowers.size() > 0 && invisibilityPowers.stream().noneMatch(InvisibilityPower::shouldRenderOutline)) {
            return false;
        }
        return original;
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void apoli$storePowersInState(T livingEntity, S livingEntityRenderState, float f, CallbackInfo ci) {
        ((ApoliLivingEntityRenderState) livingEntityRenderState).apoli$setPowerHolder(PowerHolderComponent.KEY.getNullable(livingEntity));
    }

    @ModifyArg(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getRenderType(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/renderer/rendertype/RenderType;"), index = 1)
    private boolean changeRenderLayerWhenTranslucent(boolean original, @Local(argsOnly = true) S renderState) {
        if(!renderState.isInvisibleToPlayer && !renderState.isInvisible && PowerHolderComponent.getPowers(renderState, ModelColorPower.class).stream().anyMatch(ModelColorPower::isTranslucent)) {
            return true;
        }

        return original;
    }

    @WrapWithCondition(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/EntityRenderState;FF)V"))
    private <S extends EntityRenderState> boolean preventFeatureRendering(RenderLayer featureRenderer, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, S renderState, float v, float unknown) {
        List<InvisibilityPower> invisibilityPowers = PowerHolderComponent.getPowers((LivingEntityRenderState) renderState, InvisibilityPower.class);
        if(invisibilityPowers.size() > 0 && invisibilityPowers.stream().noneMatch(InvisibilityPower::shouldRenderArmor)) {
            return false;
        }
        Class cls = featureRenderer.getClass();
        return !PowerHolderComponent.getPowers((LivingEntityRenderState) renderState, PreventFeatureRenderPower.class).stream().anyMatch(p -> p.doesApply(cls));
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "getModelTint", at = @At("HEAD"), cancellable = true)
    private void renderColorChangedModel(S renderState, CallbackInfoReturnable<Integer> cir) {
        List<ModelColorPower> modelColorPowers = PowerHolderComponent.getPowers(renderState, ModelColorPower.class);
        if (modelColorPowers.size() > 0) {
            float r = modelColorPowers.stream().map(ModelColorPower::getRed).reduce((a, b) -> a * b).get();
            float g = modelColorPowers.stream().map(ModelColorPower::getGreen).reduce((a, b) -> a * b).get();
            float b = modelColorPowers.stream().map(ModelColorPower::getBlue).reduce((a, c) -> a * c).get();
            float a = modelColorPowers.stream().map(ModelColorPower::getAlpha).min(Float::compare).get();

            cir.setReturnValue(ARGB.colorFromFloat(a, r, g, b));
        }
    }
}

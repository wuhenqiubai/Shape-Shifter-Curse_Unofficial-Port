package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.InvisibilityPower;
import io.github.apace100.apoli.power.ModelColorPower;
import io.github.apace100.apoli.power.PreventFeatureRenderPower;
import io.github.apace100.apoli.power.ShakingPower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> {

    protected LivingEntityRendererMixin(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Inject(method = "isShaking", at = @At("HEAD"), cancellable = true)
    private void letPlayersShakeTheirBodies(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if(PowerHolderComponent.hasPower(entity, ShakingPower.class)) {
            cir.setReturnValue(true);
        }
    }

    @ModifyVariable(method = "render", at = @At(value = "STORE"), ordinal = 2)
    private boolean preventOutlineRendering(boolean original, LivingEntity livingEntity) {
        List<InvisibilityPower> invisibilityPowers = PowerHolderComponent.getPowers(livingEntity, InvisibilityPower.class);
        if(invisibilityPowers.size() > 0 && invisibilityPowers.stream().noneMatch(InvisibilityPower::shouldRenderOutline)) {
            return false;
        }
        return original;
    }

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;", shift = At.Shift.BEFORE))
    private RenderType changeRenderLayerWhenTranslucent(RenderType original, LivingEntity entity) {
        if(entity != null) {
            if(PowerHolderComponent.getPowers(entity, ModelColorPower.class).stream().anyMatch(ModelColorPower::isTranslucent)) {
                return RenderType.itemEntityTranslucentCull(getTextureLocation((T) entity));
            }
        }
        return original;
    }

    @WrapOperation(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/Entity;FFFFFF)V"))
    private void preventFeatureRendering(RenderLayer featureRenderer, PoseStack matrices, MultiBufferSource vertexConsumers, int light, Entity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch, Operation<Void> original, LivingEntity livingEntity) {
        List<InvisibilityPower> invisibilityPowers = PowerHolderComponent.getPowers(livingEntity, InvisibilityPower.class);
        if(invisibilityPowers.size() > 0 && invisibilityPowers.stream().noneMatch(InvisibilityPower::shouldRenderArmor)) {
            return;
        }
        Class cls = featureRenderer.getClass();
        if(!PowerHolderComponent.getPowers(entity, PreventFeatureRenderPower.class).stream().anyMatch(p -> p.doesApply(cls))) {
            original.call(featureRenderer, matrices, vertexConsumers, light, entity, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
        }
    }

    @Environment(EnvType.CLIENT)
    @ModifyArg(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"), index = 4)
    private int renderColorChangedModel(int rgb, @Local(argsOnly = true) LivingEntity entity) {
        List<ModelColorPower> modelColorPowers = PowerHolderComponent.getPowers(entity, ModelColorPower.class);
        if (modelColorPowers.size() > 0) {
            float r = modelColorPowers.stream().map(ModelColorPower::getRed).reduce((a, b) -> a * b).get();
            float g = modelColorPowers.stream().map(ModelColorPower::getGreen).reduce((a, b) -> a * b).get();
            float b = modelColorPowers.stream().map(ModelColorPower::getBlue).reduce((a, c) -> a * c).get();
            float a = modelColorPowers.stream().map(ModelColorPower::getAlpha).min(Float::compare).get();

            return FastColor.ARGB32.colorFromFloat((FastColor.ARGB32.alpha(rgb) / 255f) * a, (FastColor.ARGB32.red(rgb) / 255f) * r, (FastColor.ARGB32.green(rgb) / 255f) * g, (FastColor.ARGB32.blue(rgb) / 255f) * b);
        }

        return rgb;
    }

}

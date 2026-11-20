package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModelColorPower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.UvMapping;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(AvatarRenderer.class)
public class PlayerEntityRendererMixin {

    @Environment(EnvType.CLIENT)
    @WrapOperation(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModelPart(Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IILnet/minecraft/client/renderer/texture/UvMapping;)V"))
    private void makeArmAndSleeveTranslucent(SubmitNodeCollector instance, ModelPart modelPart, PoseStack matrices, RenderType renderType, int light, int overlay, UvMapping uvMapping, Operation<Void> original) {
        List<ModelColorPower> modelColorPowers = PowerHolderComponent.KEY.get(Minecraft.getInstance().player).getPowers(ModelColorPower.class);
        if (modelColorPowers.size() > 0) {
            float red = modelColorPowers.stream().map(ModelColorPower::getRed).reduce((a, b) -> a * b).get();
            float green = modelColorPowers.stream().map(ModelColorPower::getGreen).reduce((a, b) -> a * b).get();
            float blue = modelColorPowers.stream().map(ModelColorPower::getBlue).reduce((a, b) -> a * b).get();
            float alpha = modelColorPowers.stream().map(ModelColorPower::getAlpha).min(Float::compare).get();
            instance.submitModelPart(modelPart, matrices, renderType, light, overlay, uvMapping, ARGB.colorFromFloat(alpha, red, green, blue), 0);
            return;
        }

        original.call(instance, modelPart, matrices, renderType, light, overlay, uvMapping);
    }
}

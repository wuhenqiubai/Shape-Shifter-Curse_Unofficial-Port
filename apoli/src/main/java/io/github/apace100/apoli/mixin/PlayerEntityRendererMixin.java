package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModelColorPower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.FastColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(PlayerRenderer.class)
public class PlayerEntityRendererMixin {

    @Environment(EnvType.CLIENT)
    @WrapOperation(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
    private void makeArmAndSleeveTranslucent(ModelPart modelPart, PoseStack matrices, VertexConsumer vertices, int light, int overlay, Operation<Void> original) {
        List<ModelColorPower> modelColorPowers = PowerHolderComponent.KEY.get(Minecraft.getInstance().player).getPowers(ModelColorPower.class);
        if (modelColorPowers.size() > 0) {
            float red = modelColorPowers.stream().map(ModelColorPower::getRed).reduce((a, b) -> a * b).get();
            float green = modelColorPowers.stream().map(ModelColorPower::getGreen).reduce((a, b) -> a * b).get();
            float blue = modelColorPowers.stream().map(ModelColorPower::getBlue).reduce((a, b) -> a * b).get();
            float alpha = modelColorPowers.stream().map(ModelColorPower::getAlpha).min(Float::compare).get();
            modelPart.render(matrices, vertices, light, overlay, FastColor.ARGB32.colorFromFloat(alpha, red, green, blue));
            return;
        }

        original.call(modelPart, matrices, vertices, light, overlay);
    }
}

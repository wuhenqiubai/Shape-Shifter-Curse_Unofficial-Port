package net.onixary.shapeShifterCurseFabric.mixin.render;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormModel;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormRenderUtils;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = LivingEntityRenderer.class, priority = 10000)
public abstract class OverlayRenderMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @Shadow
    protected M model;

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V",
                    shift = At.Shift.AFTER))
    private void renderFormOverlay(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, CallbackInfo ci) {
        if (!(livingEntity instanceof AbstractClientPlayerEntity player)) return;
        if (player.isInvisible() || player.isSpectator()) return;
        if (!(((Object) this) instanceof PlayerEntityRenderer playerEntityRenderer)) return;

	    PlayerEntityModel<AbstractClientPlayerEntity> playerEntityModel = playerEntityRenderer.getModel();

        List<FormRenderer> formRendererList = FormRenderUtils.getPlayerAllFormRenderer(player);
        for (FormRenderer formRenderer : formRendererList) {
            if (formRenderer == null) continue;
            FormModel formModel = (FormModel) formRenderer.getGeoModel();
            if (formModel == null) continue;

            float hurtTime = player.hurtTime > 0 ? player.hurtTime - g : 0;
            int overlay = OverlayTexture.packUv(
                    OverlayTexture.getU(hurtTime),
                    OverlayTexture.getV(player.hurtTime > 0 || player.deathTime > 0));

            Identifier overlayTexture = formModel.getOverlayTextureResource(playerEntityModel.thinArms);
            if (overlayTexture != null) {
                RenderLayer renderLayer = RenderLayer.getEntityTranslucent(overlayTexture);
                playerEntityModel.render(matrixStack, vertexConsumerProvider.getBuffer(renderLayer),
                        light, overlay, 0xFFFFFFFF);
            }

            Identifier emissiveTexture = formModel.getEmissiveTextureResource(playerEntityModel.thinArms);
            if (emissiveTexture != null) {
                RenderLayer renderLayer = RenderLayer.getEntityTranslucentEmissive(emissiveTexture);
                playerEntityModel.render(matrixStack, vertexConsumerProvider.getBuffer(renderLayer),
                        light, overlay, 0xFFFFFFFF);
            }
        }
    }
}

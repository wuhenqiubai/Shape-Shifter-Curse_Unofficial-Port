package net.onixary.shapeShifterCurseFabric.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
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
public abstract class OverlayRenderMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {

    @Shadow
    protected M model;

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
                    shift = At.Shift.AFTER))
    private void renderFormOverlay(T livingEntity, float f, float g, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, CallbackInfo ci) {
        if (!(livingEntity instanceof AbstractClientPlayer player)) return;
        if (player.isInvisible() || player.isSpectator()) return;
        if (!(((Object) this) instanceof AvatarRenderer playerEntityRenderer)) return;

        PlayerModel playerEntityModel = playerEntityRenderer.getModel();

        List<FormRenderer> formRendererList = FormRenderUtils.getPlayerAllFormRenderer(player);
        for (FormRenderer formRenderer : formRendererList) {
            if (formRenderer == null) continue;
            FormModel formModel = (FormModel) formRenderer.getGeoModel();
            if (formModel == null) continue;

            float hurtTime = player.hurtTime > 0 ? player.hurtTime - g : 0;
            int overlay = OverlayTexture.pack(
                    OverlayTexture.u(hurtTime),
                    OverlayTexture.v(player.hurtTime > 0 || player.deathTime > 0));

            Identifier overlayTexture = formModel.getOverlayTextureResource(playerEntityModel.slim);
            if (overlayTexture != null) {
                RenderType renderLayer = RenderType.entityTranslucent(overlayTexture);
                playerEntityModel.renderToBuffer(matrixStack, vertexConsumerProvider.getBuffer(renderLayer),
                        light, overlay, 0xFFFFFFFF);
            }

            Identifier emissiveTexture = formModel.getEmissiveTextureResource(playerEntityModel.slim);
            if (emissiveTexture != null) {
                RenderType renderLayer = RenderType.entityTranslucentEmissive(emissiveTexture);
                playerEntityModel.renderToBuffer(matrixStack, vertexConsumerProvider.getBuffer(renderLayer),
                        light, overlay, 0xFFFFFFFF);
            }
        }
    }
}
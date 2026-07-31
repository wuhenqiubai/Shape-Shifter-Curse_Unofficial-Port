package net.onixary.shapeShifterCurseFabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormModel;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormRenderUtils;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormRenderer;
import net.onixary.shapeShifterCurseFabric.render.form_render.IModifyHead_MAS;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// NECK FEATURES FILE

@Mixin(CustomHeadLayer.class)
public class LongNeckHeadFeatureRendererMixin<S extends LivingEntityRenderState, M extends EntityModel<S> & HeadedModel> {
    @Unique private static FormRenderUtils.BoneBipedState headBoneState;

    // 1.21.11 渲染状态中不再直接携带实体，通过 AvatarRenderState 的实体 id 反查当前正在渲染的玩家
    @Unique
    private static AbstractClientPlayer getRenderedPlayer(LivingEntityRenderState livingEntityRenderState) {
        if (livingEntityRenderState instanceof AvatarRenderState avatarRenderState && Minecraft.getInstance().level != null) {
            if (Minecraft.getInstance().level.getEntity(avatarRenderState.id) instanceof AbstractClientPlayer player) {
                return player;
            }
        }
        return null;
    }

    @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;FF)V", at = @At("HEAD"), cancellable = true)
    private void shape_shifter_curse$modifyHeadStateForMAS(PoseStack matrixStack, SubmitNodeCollector vertexConsumerProvider, int light, S livingEntityRenderState, float limbAngle, float limbDistance, CallbackInfo ci) {
        AbstractClientPlayer player = getRenderedPlayer(livingEntityRenderState);
        if (player == null) {
            return;
        }
        AvatarRenderer playerEntityRenderer = (AvatarRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
        FormRenderer renderer = FormRenderUtils.searchFirstRenderer(player, formRenderer -> {
            FormModel formModel = formRenderer.realModel;
            if (formModel == null) {
                return false;
            }
            return formModel.AnimationSystem instanceof IModifyHead_MAS;
        });
        if (renderer != null) {
            // AvatarRenderer 原类型下 getModel() 返回原始类型 EntityModel，玩家模型实际是 HumanoidModel，这里转回以便操作头部
            HumanoidModel<?> playerModel = (HumanoidModel<?>) playerEntityRenderer.getModel();
            headBoneState = new FormRenderUtils.BoneBipedState(playerModel.getHead());
            ((IModifyHead_MAS)renderer.realModel.AnimationSystem).modifyHeadPart(player, playerModel, renderer.realModel);
        }
    }

    @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;FF)V", at = @At("RETURN"))
    private void shape_shifter_curse$restoreVanillaHeadFeatureForLongNeck(PoseStack matrixStack, SubmitNodeCollector vertexConsumerProvider, int light, S livingEntityRenderState, float limbAngle, float limbDistance, CallbackInfo ci) {
        if (headBoneState != null) {
            headBoneState.restore();
            headBoneState = null;
        }
    }
}

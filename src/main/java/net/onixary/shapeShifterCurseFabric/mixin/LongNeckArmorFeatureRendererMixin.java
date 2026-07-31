package net.onixary.shapeShifterCurseFabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormModel;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormRenderUtils;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormRenderer;
import net.onixary.shapeShifterCurseFabric.render.form_render.IModifyHead_MAS;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// NECK FEATURES FILE

@Mixin(HumanoidArmorLayer.class)
public class LongNeckArmorFeatureRendererMixin<S extends HumanoidRenderState, M extends HumanoidModel<S>, A extends HumanoidModel<S>> {
    @Unique private static FormRenderUtils.BoneBipedState headBoneState;

    // 1.21.11 渲染状态中不再直接携带实体，通过 AvatarRenderState 的实体 id 反查当前正在渲染的玩家
    @Unique
    private static AbstractClientPlayer getRenderedPlayer(HumanoidRenderState humanoidRenderState) {
        if (humanoidRenderState instanceof AvatarRenderState avatarRenderState && Minecraft.getInstance().level != null) {
            if (Minecraft.getInstance().level.getEntity(avatarRenderState.id) instanceof AbstractClientPlayer player) {
                return player;
            }
        }
        return null;
    }

    // 1.21.11 的 renderArmorPiece 不再暴露 armor model，改为在 getArmorModel 返回后立刻修改头部姿态，
    // 再在 renderArmorPiece 返回时恢复，以保持原逻辑（渲染护甲前改头、渲染完成后还原）
    @Inject(
            method = "getArmorModel(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/client/model/HumanoidModel;",
            at = @At("RETURN")
    )
    private void shape_shifter_curse$modifyHeadStateForMAS(S humanoidRenderState, EquipmentSlot armorSlot, CallbackInfoReturnable<A> cir) {
        A model = cir.getReturnValue();
        if (model == null) {
            return;
        }
        AbstractClientPlayer player = getRenderedPlayer(humanoidRenderState);
        if (player == null) {
            return;
        }
        FormRenderer renderer = FormRenderUtils.searchFirstRenderer(player, formRenderer -> {
            FormModel formModel = formRenderer.realModel;
            if (formModel == null) {
                return false;
            }
            return formModel.AnimationSystem instanceof IModifyHead_MAS;
        });
        if (renderer != null) {
            headBoneState = new FormRenderUtils.BoneBipedState(model.getHead());
            ((IModifyHead_MAS)renderer.realModel.AnimationSystem).modifyHeadPart(player, model, renderer.realModel);
        }
    }

    @Inject(method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At(value = "RETURN"))
    private void shape_shifter_curse$restoreHeadStateForMAS(PoseStack matrices, SubmitNodeCollector vertexConsumers, ItemStack itemStack, EquipmentSlot armorSlot, int light, S humanoidRenderState, CallbackInfo ci) {
        if (headBoneState != null) {
            headBoneState.restore();
            headBoneState = null;
        }
    }


}

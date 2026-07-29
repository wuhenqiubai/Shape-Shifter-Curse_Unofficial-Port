package net.onixary.shapeShifterCurseFabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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

@Mixin(HumanoidArmorLayer.class)
public class LongNeckArmorFeatureRendererMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> {
    @Unique private static FormRenderUtils.BoneBipedState headBoneState;

    @Inject(
            method = "renderArmorPiece",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/HumanoidModel;copyPropertiesTo(Lnet/minecraft/client/model/HumanoidModel;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void shape_shifter_curse$modifyHeadStateForMAS(PoseStack matrices, MultiBufferSource vertexConsumers, T entity, EquipmentSlot armorSlot, int light, A model, CallbackInfo ci) {
        if (!(entity instanceof AbstractClientPlayer player)) {
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
            headBoneState = new FormRenderUtils.BoneBipedState(model.getHead());
            ((IModifyHead_MAS)renderer.realModel.AnimationSystem).modifyHeadPart(player, model, renderer.realModel);
        }
    }

    @Inject(method = "renderArmorPiece", at = @At(value = "RETURN"))
    private void shape_shifter_curse$restoreHeadStateForMAS(PoseStack matrices, MultiBufferSource vertexConsumers, T entity, EquipmentSlot armorSlot, int light, A model, CallbackInfo ci) {
        if (headBoneState != null) {
            headBoneState.restore();
            headBoneState = null;
        }
    }


}
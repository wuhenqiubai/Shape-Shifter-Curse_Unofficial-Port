package net.onixary.shapeShifterCurseFabric.mixin;


import com.mojang.blaze3d.vertex.PoseStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.onixary.shapeShifterCurseFabric.additional_power.HideTPHeldItemPower;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.PlayerFormBodyType;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = ItemInHandLayer.class, priority = 99999999)
public abstract class AdjustItemHoldFeatureRendererMixin<S extends ArmedEntityRenderState, M extends EntityModel<S> & ArmedModel> {
    /*private LivingEntity cachedEntity; // 缓存 livingEntity

    @Inject(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
            at = @At("HEAD")
    )
    private void cacheLivingEntity(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            LivingEntity entity,
            float limbAngle,
            float limbDistance,
            float tickDelta,
            float animationProgress,
            float headYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        this.cachedEntity = entity; // 缓存传入的 livingEntity
    }


    @ModifyArg(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/feature/HeldItemFeatureRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;Lnet/minecraft/util/Arm;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"
            )
    )
    private MatrixStack modifyMatrixStack(MatrixStack originalMatrices) {
        // 创建一个新的 MatrixStack 或修改原始的 MatrixStack
        MatrixStack modifiedMatrices = new MatrixStack();
        modifiedMatrices.push();
        modifiedMatrices.scale(0.01F, 0.01F, 0.01F); // 添加缩放操作
        modifiedMatrices.pop();
        if (shouldHideItem(this.cachedEntity)) {
            return modifiedMatrices;
        }
        else{
            return originalMatrices;
        }

    }*/

    @Inject(at =
    @At(value = "HEAD"),
            method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/ArmedEntityRenderState;FF)V",
            cancellable = true
    )
    private void hideHeldItem(
            PoseStack matrices,
            SubmitNodeCollector vertexConsumers,
            int light,
            S armedEntityRenderState,
            float limbAngle,
            float limbDistance,
            CallbackInfo ci
    ) {
        // 条件判断：例如隐藏特定玩家或满足条件时

        if (shouldHideItem(armedEntityRenderState)) {
            ci.cancel(); // 取消原版渲染逻辑
        }
    }

    private boolean shouldHideItem(S armedEntityRenderState) {
        // 1.21.11 渲染状态中无法直接拿到实体，通过 AvatarRenderState 的实体 id 反查
        if (armedEntityRenderState instanceof AvatarRenderState avatarRenderState
                && Minecraft.getInstance().level != null
                && Minecraft.getInstance().level.getEntity(avatarRenderState.id) instanceof AbstractClientPlayer player) {
            IForm curForm = FormTextureUtils.getPlayerForm_Render(player);
            boolean isFeral = curForm.getBodyType() == PlayerFormBodyType.FERAL;
            //ShapeShifterCurseFabric.LOGGER.info("Is Feral Form : " + isFeral);

            boolean hasHideTPItemPower = PowerHolderComponent.hasPower(player, HideTPHeldItemPower.class);
            return isFeral || hasHideTPItemPower;
        }
        return false;
    }
}

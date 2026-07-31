package net.onixary.shapeShifterCurseFabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.onixary.shapeShifterCurseFabric.player_form.PlayerFormBodyType;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 这一mixin与ViveCraft mod冲突，当其存在时禁用此mixin
// This mixin conflicts with the ViveCraft mod, disable this mixin when it exists
@Mixin(AvatarRenderer.class)
public abstract class PlayerEntityRendererFallFlyingMixin extends LivingEntityRenderer<AbstractClientPlayer, AvatarRenderState, PlayerModel> {

    public PlayerEntityRendererFallFlyingMixin(EntityRendererProvider.Context ctx, PlayerModel model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    // 1.21.11 渲染状态中不再直接携带实体，通过 AvatarRenderState 的实体 id 反查当前正在渲染的玩家
    @Unique
    private static AbstractClientPlayer ssc$getRenderedPlayer(AvatarRenderState avatarRenderState) {
        if (Minecraft.getInstance().level != null) {
            if (Minecraft.getInstance().level.getEntity(avatarRenderState.id) instanceof AbstractClientPlayer player) {
                return player;
            }
        }
        return null;
    }

    @Inject(method = "setupRotations(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V",
            at = @At("HEAD"),
            cancellable = true
    )
    public void setupTransformsInject(AvatarRenderState avatarRenderState, PoseStack poseStack, float f, float g, CallbackInfo ci) {
        // 如果 vivecraft 对此Inject仍不兼容 把下面代码解除注释
        /*
        if (FabricLoader.getInstance().isModLoaded("vivecraft")) {
            ShapeShifterCurseFabric.LOGGER.info("ViveCraft mod detected, skipping PlayerEntityRendererFallFlyingMixin.");
            return;
        }
        */
        if (!avatarRenderState.isFallFlying) {
            return;
        }
        AbstractClientPlayer abstractClientPlayerEntity = ssc$getRenderedPlayer(avatarRenderState);
        if (abstractClientPlayerEntity == null) {
            return;
        }
        boolean isFeral = FormTextureUtils.getPlayerForm_Render(abstractClientPlayerEntity).getBodyType() == PlayerFormBodyType.FERAL;
        if(!isFeral){
            return;
        }
        else{
            // 1.21.11 中飞行偏航角已由 extractFlightData 预计算进渲染状态（flyingYRot），
            // 这里将其转回角度制以保持原有 Feral 形态的旋转行为
            super.setupRotations(avatarRenderState, poseStack, f, g);
            poseStack.mulPose(Axis.YP.rotationDegrees((float)Math.toDegrees(avatarRenderState.flyingYRot)));
            poseStack.mulPose(Axis.XP.rotationDegrees(0.0F)); // 不向下倾斜
            // 你可以在这里添加任何额外的旋转
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F)); // 使翅膀向上
            ci.cancel();
        }
    }

    // 老逻辑
    /*
     * @author OnyxAmber
     * @reason Feral form elytra cancel rotation
     */
    /* @Overwrite
    public void setupTransforms(AbstractClientPlayerEntity player, MatrixStack matrices, float f, float g, float h) {
        // 这一mixin与ViveCraft mod冲突，当其存在时禁用此mixin
        // This mixin conflicts with the ViveCraft mod, disable this mixin when it exists
        boolean IS_VIVECRAFT_LOADED = FabricLoader.getInstance().isModLoaded("vivecraft");
        if (IS_VIVECRAFT_LOADED) {
            super.setupTransforms(player, matrices, f, g, h);
            ShapeShifterCurseFabric.LOGGER.info("ViveCraft mod detected, skipping PlayerEntityRendererFallFlyingMixin.");
            return;
        }
        IForm curForm = RegPlayerFormComponent.PLAYER_FORM.get(player).getCurrentForm();
        boolean isFeral = curForm.getBodyType() == PlayerFormBodyType.FERAL;

        float i = player.getLeaningPitch(h);
        if (player.isFallFlying()) {
            super.setupTransforms(player, matrices, f, g, h);
            float j = (float)player.getRoll() + h;
            float k = MathHelper.clamp(j * j / 100.0f, 0.0f, 1.0f);

            if (!player.isUsingRiptide()) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(k * (-90.0f - player.getPitch())));
            }
            Vec3d vec3d = player.getRotationVec(h);
            Vec3d vec3d2 = player.lerpVelocity(h);
            double d = vec3d2.horizontalLengthSquared();
            double e = vec3d.horizontalLengthSquared();
            if (d > 0.0 && e > 0.0) {
                double l = (vec3d2.x * vec3d.x + vec3d2.z * vec3d.z) / Math.sqrt(d * e);
                double m = vec3d2.x * vec3d.z - vec3d2.z * vec3d.x;
                if(!isFeral){
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotation((float)(Math.signum(m) * Math.acos(l))));
                }
                else{
                    // Feral形态的特殊旋转
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(Math.signum(m) * Math.acos(l)) * 180.0F / (float)Math.PI));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(0.0F)); // 不向下倾斜
                    // 你可以在这里添加任何额外的旋转
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F)); // 使翅膀向上
                }
            }
        }
        else if (i > 0.0f) {
            super.setupTransforms(player, matrices, f, g, h);
            float j = player.isTouchingWater() ? -90.0f - player.getPitch() : -90.0f;
            float k = MathHelper.lerp(i, 0.0f, j);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(k));
            if (player.isInSwimmingPose()) {
                matrices.translate(0.0f, -1.0f, 0.3f);
            }
        } else {
            super.setupTransforms(player, matrices, f, g, h);
        }
    }
    */
}

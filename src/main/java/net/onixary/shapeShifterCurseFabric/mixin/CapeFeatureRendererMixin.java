package net.onixary.shapeShifterCurseFabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.PlayerFormBodyType;
import net.onixary.shapeShifterCurseFabric.player_form.utils.ModifyCapeRender;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(CapeLayer.class)
public class CapeFeatureRendererMixin {

    @Unique
    private AbstractClientPlayer ssc$capePlayer;

    // 1.21.11 CapeLayer 改为 submit(AvatarRenderState) 模式，从 avatarRenderState.id 恢复实体
    @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V", at = @At("HEAD"))
    private void capturePlayer(PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                               int i, AvatarRenderState avatarRenderState, float f, float g, CallbackInfo ci) {
        Entity entity = Minecraft.getInstance().level.getEntity(avatarRenderState.id);
        this.ssc$capePlayer = entity instanceof AbstractClientPlayer player ? player : null;
    }

    @ModifyArg(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"),
            index = 1)
    private float modifyTranslateY(float y) {
        if (ssc$capePlayer != null) {
            Vec3 idlePos = getCapeIdleLoc(ssc$capePlayer);
            return (float) idlePos.y;
        }
        return y;
    }

    @ModifyArg(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"),
            index = 2)
    private float modifyTranslateZ(float z) {
        if (ssc$capePlayer != null) {
            Vec3 idlePos = getCapeIdleLoc(ssc$capePlayer);
            return (float) idlePos.z;
        }
        return z;
    }

    // 1.21.11 CapeLayer.submit 中无 mulPose 调用，改为在提交披风模型前施加向上旋转
    @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
                    shift = At.Shift.BEFORE))
    private void addCapeUpwardRotation(PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                                       int i, AvatarRenderState avatarRenderState, float f, float g, CallbackInfo ci) {
        if (ssc$capePlayer != null) {
            float baseRotation = getCapeBaseRotateAngle(ssc$capePlayer);
            poseStack.mulPose(Axis.XP.rotationDegrees(baseRotation));
        }
    }

    // TODO: 1.21.11 CapeLayer.submit 中无 Axis.rotationDegrees 调用，
    // modifyXRotationAngle（钳制披风 X 旋转角度）逻辑暂不可用，待迁移到 PlayerCapeModel.setupAnim

    // helper func
    @Unique
    private Vec3 getCapeIdleLoc(AbstractClientPlayer player) {
        IForm curForm = FormTextureUtils.getPlayerForm_Render(player);
        if (curForm instanceof ModifyCapeRender mcr) {
            return mcr.getCapeIdleLoc(player);
        }
        if (curForm.getBodyType() == PlayerFormBodyType.FERAL) {
            return new Vec3(0.0f, -0.2f, 0.3f);
        }
        else {
            return new Vec3(0.0, 0.2f, 0.125);
        }
    }

    // 获取披风的基础旋转角度
    @Unique
    private float getCapeBaseRotateAngle(AbstractClientPlayer player) {
        IForm curForm = FormTextureUtils.getPlayerForm_Render(player);
        if (curForm instanceof ModifyCapeRender mcr) {
            return mcr.getCapeBaseRotateAngle(player);
        }
        return 0.0f;
    }
}

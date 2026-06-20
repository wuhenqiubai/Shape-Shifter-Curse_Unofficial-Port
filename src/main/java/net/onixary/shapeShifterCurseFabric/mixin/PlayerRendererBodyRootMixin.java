package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.IAnimSystemAccessor;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.keyframe.BoneAnimation;
import software.bernie.geckolib.animation.keyframe.Keyframe;
import software.bernie.geckolib.animation.keyframe.KeyframeStack;
import software.bernie.geckolib.loading.math.MathValue;

/**
 * Replaces PAL's PlayerRendererMixin.applyBodyTransforms.
 * Applies bodyRoot (PAL body bone) rotation/position to the MatrixStack at setupTransforms,
 * affecting both vanilla model and Geo model rendering.
 */
@Mixin(PlayerEntityRenderer.class)
public class PlayerRendererBodyRootMixin {

    @Inject(method = "setupTransforms(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/util/math/MatrixStack;FFFF)V", at = @At("RETURN"))
    private void ssc$applyBodyRootTransform(AbstractClientPlayerEntity player, MatrixStack matrices,
                                             float animationProgress, float bodyYaw, float tickDelta, float scale, CallbackInfo ci) {
        if (!(player instanceof IAnimSystemAccessor accessor)) return;
        var animState = accessor.shape_shifter_curse$getAnimSystem().animationState;
        if (animState.currentBodyAnimId == null) return;
        Animation anim = FormModel.getCachedAnimation(animState.currentBodyAnimId.getPath());
        if (anim == null) return;

        BoneAnimation bodyRootAnim = null;
        for (BoneAnimation ba : anim.boneAnimations()) {
            if (ba.boneName().equals("bodyRoot")) { bodyRootAnim = ba; break; }
        }
        if (bodyRootAnim == null) return;

        double elapsed = (player.age - animState.bodyAnimStartAge + tickDelta) * animState.bodySpeed;

        KeyframeStack<Keyframe<MathValue>> posFrames = bodyRootAnim.positionKeyFrames();
        KeyframeStack<Keyframe<MathValue>> rotFrames = bodyRootAnim.rotationKeyFrames();

        // Apply position: PAL body bone pivot at Y=12 → translate(-posX/16, posY/16 + 0.75, posZ/16)
        if (!posFrames.xKeyframes().isEmpty()) {
            float px = (float)FormModel.interpolateValue(FormModel.findKeyframe(posFrames.xKeyframes(), elapsed));
            float py = (float)FormModel.interpolateValue(FormModel.findKeyframe(posFrames.yKeyframes(), elapsed));
            float pz = (float)FormModel.interpolateValue(FormModel.findKeyframe(posFrames.zKeyframes(), elapsed));
            matrices.translate(-px / 16f, py / 16f + 0.75f, pz / 16f);
        } else {
            matrices.translate(0, 0.75f, 0);
        }

        // Apply rotation: match PAL's body bone transform (body.rotX*=-1, body.rotY*=-1, rotateZYX)
        // GeckoLib already stores X/Y as negated radians, so use directly for X, un-negate for Y
        if (!rotFrames.xKeyframes().isEmpty()) {
            float rx = (float)FormModel.interpolateValue(FormModel.findKeyframe(rotFrames.xKeyframes(), elapsed)); // already negated by GeckoLib, matches PAL rotX*=-1
            float ry = -(float)FormModel.interpolateValue(FormModel.findKeyframe(rotFrames.yKeyframes(), elapsed)); // un-negate GeckoLib, then invertY below
            float rz = (float)FormModel.interpolateValue(FormModel.findKeyframe(rotFrames.zKeyframes(), elapsed));
            ry = -ry; // PAL body.rotY *= -1
            matrices.multiply(RotationAxis.POSITIVE_Z.rotation(rz));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(ry));
            matrices.multiply(RotationAxis.POSITIVE_X.rotation(rx));
        }

        matrices.translate(0, -0.75f, 0); // undo pivot offset
    }
}

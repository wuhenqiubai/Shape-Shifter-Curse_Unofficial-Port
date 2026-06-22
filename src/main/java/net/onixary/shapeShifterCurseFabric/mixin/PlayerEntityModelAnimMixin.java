package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.Entity;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimationState;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.IAnimSystemAccessor;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.keyframe.BoneAnimation;
import software.bernie.geckolib.animation.keyframe.Keyframe;
import software.bernie.geckolib.animation.keyframe.KeyframeLocation;
import software.bernie.geckolib.animation.keyframe.KeyframeStack;
import software.bernie.geckolib.loading.math.MathValue;

/**
 * Replaces PAL's PlayerModelMixin. Resets ModelPart pivots at HEAD of setAngles,
 * then applies form animation keyframes at RETURN using PAL's translatePartToBone formula.
 * This feeds into DefaultModelAnimationSystem.processAnimation which reads ModelParts.
 */
@Mixin(PlayerEntityModel.class)
public class PlayerEntityModelAnimMixin {

    @Inject(method = "setAngles(Lnet/minecraft/entity/Entity;FFFFF)V", at = @At("HEAD"))
    private void ssc$resetPartsToDefault(Entity entity, float limbSwing, float limbSwingAmount,
                                          float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof AbstractClientPlayerEntity)) return;
        PlayerEntityModel<?> self = (PlayerEntityModel<?>) (Object) this;
        self.head.resetTransform();
        self.body.resetTransform();
        self.rightArm.resetTransform();
        self.leftArm.resetTransform();
        self.rightLeg.resetTransform();
        self.leftLeg.resetTransform();
        self.hat.resetTransform();
        self.jacket.resetTransform();
        self.rightSleeve.resetTransform();
        self.leftSleeve.resetTransform();
        self.rightPants.resetTransform();
        self.leftPants.resetTransform();
    }

    @Inject(method = "setAngles(Lnet/minecraft/entity/Entity;FFFFF)V", at = @At("RETURN"))
    private void ssc$applyFormAnimation(Entity entity, float limbSwing, float limbSwingAmount,
                                         float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof AbstractClientPlayerEntity player)) return;
        if (!(player instanceof IAnimSystemAccessor accessor)) return;

        AnimationState animState = accessor.shape_shifter_curse$getAnimSystem().animationState;
        if (animState.currentBodyAnimId == null) return;

        Animation anim = FormModel.getCachedAnimation(animState.currentBodyAnimId.getPath());
        if (anim == null) return;

        double elapsed = (ageInTicks - animState.bodyAnimStartAge) * animState.bodySpeed;
        double animLength = FormModel.getAnimLength(anim);
        if (anim.loopType().shouldPlayAgain(null, null, anim) && animLength > 0) {
            elapsed = elapsed % animLength;
        } else if (elapsed > animLength && animLength > 0) {
            elapsed = animLength - 0.01;
        }
        String easingType = animState.easingTypeName;

        PlayerEntityModel<?> self = (PlayerEntityModel<?>) (Object) this;

        for (BoneAnimation boneAnim : anim.boneAnimations()) {
            ModelPart part = FormModel.getVanillaPart(self, boneAnim.boneName());
            if (part == null) continue;

            KeyframeStack<Keyframe<MathValue>> rotFrames = boneAnim.rotationKeyFrames();
            KeyframeStack<Keyframe<MathValue>> posFrames = boneAnim.positionKeyFrames();

            if (!posFrames.xKeyframes().isEmpty()) {
                KeyframeLocation<Keyframe<MathValue>> locX = FormModel.findKeyframe(posFrames.xKeyframes(), elapsed);
                KeyframeLocation<Keyframe<MathValue>> locY = FormModel.findKeyframe(posFrames.yKeyframes(), elapsed);
                KeyframeLocation<Keyframe<MathValue>> locZ = FormModel.findKeyframe(posFrames.zKeyframes(), elapsed);
                float kx = (float)FormModel.interpolateValue(locX, easingType);
                float ky = (float)FormModel.interpolateValue(locY, easingType);
                float kz = (float)FormModel.interpolateValue(locZ, easingType);
                // Undo (-x, y, -z) processing applied to animation data (AC works with processed data; Mixin needs raw PAL values)
                kx = -kx; kz = -kz;
                ModelTransform def = part.getDefaultTransform();
                float dpX = def.pivotX;
                float dpY = def.pivotY;
                float dpZ = def.pivotZ;
                part.pivotX = kx + dpX;
                part.pivotY = -ky + dpY;
                part.pivotZ = kz + dpZ;
            }

            if (!rotFrames.xKeyframes().isEmpty()) {
                KeyframeLocation<Keyframe<MathValue>> locX = FormModel.findKeyframe(rotFrames.xKeyframes(), elapsed);
                KeyframeLocation<Keyframe<MathValue>> locY = FormModel.findKeyframe(rotFrames.yKeyframes(), elapsed);
                KeyframeLocation<Keyframe<MathValue>> locZ = FormModel.findKeyframe(rotFrames.zKeyframes(), elapsed);
                float rx = (float)FormModel.interpolateValue(locX, easingType);
                float ry = (float)FormModel.interpolateValue(locY, easingType);
                float rz = (float)FormModel.interpolateValue(locZ, easingType);
                // Undo (-x, y, -z) processing: X and Z need to be reverted
                rx = -rx; rz = -rz;
                part.pitch = -rx;
                part.yaw = -ry;
                part.roll = rz;
            }
        }

        // Sync overlay parts after form animation is applied
        self.leftPants.copyTransform(self.leftLeg);
        self.rightPants.copyTransform(self.rightLeg);
        self.leftSleeve.copyTransform(self.leftArm);
        self.rightSleeve.copyTransform(self.rightArm);
        self.jacket.copyTransform(self.body);
        self.hat.copyTransform(self.head);
    }
}

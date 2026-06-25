package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormModel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Synchronises GeoBone transforms back to vanilla ModelParts after GeckoLib animation.
 * This ensures armor, held items, trinkets, and mods like BetterCombat
 * see the correct bone positions even though we render through pure Geo.
 *
 * The sync runs at the end of setAngles with a 1-frame cache: on frame N,
 * we read the GeoBone transforms that were computed during frame N-1's
 * FormRenderFeature pass and write them to ModelParts for frame N's
 * equipment/layer rendering.  The 1-frame delay is imperceptible at 60fps.
 */
@Mixin(PlayerEntityModel.class)
public abstract class PlayerEntityModelAnimSyncMixin<T extends LivingEntity> {

    @Unique @Final public ModelPart head;
    @Unique @Final public ModelPart body;
    @Unique @Final public ModelPart leftArm;
    @Unique @Final public ModelPart rightArm;
    @Unique @Final public ModelPart leftLeg;
    @Unique @Final public ModelPart rightLeg;

    @Unique private static float ssc$cachedHeadX, ssc$cachedHeadY, ssc$cachedHeadZ;
    @Unique private static float ssc$cachedBodyX, ssc$cachedBodyY, ssc$cachedBodyZ;
    @Unique private static float ssc$cachedLArmX, ssc$cachedLArmY, ssc$cachedLArmZ;
    @Unique private static float ssc$cachedRArmX, ssc$cachedRArmY, ssc$cachedRArmZ;
    @Unique private static float ssc$cachedLLegX, ssc$cachedLLegY, ssc$cachedLLegZ;
    @Unique private static float ssc$cachedRLegX, ssc$cachedRLegY, ssc$cachedRLegZ;
    @Unique private static boolean ssc$hasCached = false;

    @Inject(method = "setAngles", at = @At("RETURN"))
    private void ssc$applyCachedBoneTransforms(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (!ssc$hasCached) return;
        head.pitch = ssc$cachedHeadX;   head.yaw   = ssc$cachedHeadY;   head.roll  = ssc$cachedHeadZ;
        body.pitch = ssc$cachedBodyX;   body.yaw   = ssc$cachedBodyY;   body.roll  = ssc$cachedBodyZ;
        leftArm.pitch = ssc$cachedLArmX; leftArm.yaw = ssc$cachedLArmY; leftArm.roll = ssc$cachedLArmZ;
        rightArm.pitch = ssc$cachedRArmX; rightArm.yaw = ssc$cachedRArmY; rightArm.roll = ssc$cachedRArmZ;
        leftLeg.pitch = ssc$cachedLLegX; leftLeg.yaw = ssc$cachedLLegY; leftLeg.roll = ssc$cachedLLegZ;
        rightLeg.pitch = ssc$cachedRLegX; rightLeg.yaw = ssc$cachedRLegY; rightLeg.roll = ssc$cachedRLegZ;
    }

    /** Called from FormRenderFeature after GeckoLib has animated the form model. */
    public static void ssc$cacheBoneTransforms(FormModel formModel) {
        var headBone = formModel.getBone("bipedHead").orElse(null);
        if (headBone == null) { ssc$hasCached = false; return; }
        ssc$hasCached = true;
        ssc$cachedHeadX = headBone.getRotX(); ssc$cachedHeadY = headBone.getRotY(); ssc$cachedHeadZ = headBone.getRotZ();
        var bodyBone = formModel.getBone("bipedBody").orElse(null);
        if (bodyBone != null) { ssc$cachedBodyX = bodyBone.getRotX(); ssc$cachedBodyY = bodyBone.getRotY(); ssc$cachedBodyZ = bodyBone.getRotZ(); }
        var lArm = formModel.getBone("bipedLeftArm").orElse(null);
        if (lArm != null) { ssc$cachedLArmX = lArm.getRotX(); ssc$cachedLArmY = lArm.getRotY(); ssc$cachedLArmZ = lArm.getRotZ(); }
        var rArm = formModel.getBone("bipedRightArm").orElse(null);
        if (rArm != null) { ssc$cachedRArmX = rArm.getRotX(); ssc$cachedRArmY = rArm.getRotY(); ssc$cachedRArmZ = rArm.getRotZ(); }
        var lLeg = formModel.getBone("bipedLeftLeg").orElse(null);
        if (lLeg != null) { ssc$cachedLLegX = lLeg.getRotX(); ssc$cachedLLegY = lLeg.getRotY(); ssc$cachedLLegZ = lLeg.getRotZ(); }
        var rLeg = formModel.getBone("bipedRightLeg").orElse(null);
        if (rLeg != null) { ssc$cachedRLegX = rLeg.getRotX(); ssc$cachedRLegY = rLeg.getRotY(); ssc$cachedRLegZ = rLeg.getRotZ(); }
    }
}

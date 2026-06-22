package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormModel;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormModel.BodyRootData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies bodyRoot (PAL body bone) rotation/position to the MatrixStack at setupTransforms,
 * affecting both vanilla model and Geo model rendering. BodyRoot computation is delegated
 * to {@link FormModel#computeBodyRootTransform}.
 */
@Mixin(PlayerEntityRenderer.class)
public class PlayerRendererBodyRootMixin {

    @Inject(method = "setupTransforms(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/util/math/MatrixStack;FFFF)V", at = @At("RETURN"))
    private void ssc$applyBodyRootTransform(AbstractClientPlayerEntity player, MatrixStack matrices,
                                             float animationProgress, float bodyYaw, float tickDelta, float scale, CallbackInfo ci) {
        // Only apply for mixed forms (vanilla model needs whole-body rotation; pure Geo uses AC hierarchy)
        if (!FormModel.applyBodyTransform) return;
        BodyRootData data = FormModel.computeBodyRootTransform(player, tickDelta);
        if (data == null) return;

        // Apply position: PAL body bone pivot at Y=12 → translate(-posX/16, posY/16 + 0.75, posZ/16)
        Vec3d pos = data.pos();
        if (pos != null) {
            matrices.translate(-pos.x / 16f, pos.y / 16f + 0.75f, pos.z / 16f);
        } else {
            matrices.translate(0, 0.75f, 0);
        }

        // Apply rotation directly from keyframe values (X negated for body prone convention)
        Vec3d rot = data.rot();
        if (rot != null) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotation((float) rot.z));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation((float) rot.y));
            matrices.multiply(RotationAxis.POSITIVE_X.rotation(-(float) rot.x));
        }

        matrices.translate(0, -0.75f, 0); // undo pivot offset
    }
}

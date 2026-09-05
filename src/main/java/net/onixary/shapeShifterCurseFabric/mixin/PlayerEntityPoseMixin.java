package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.commands.CommandSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.PlayerFormBodyType;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerEntityPoseMixin extends LivingEntity implements Nameable, CommandSource {

    @Shadow public abstract boolean isSwimming();

    protected PlayerEntityPoseMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "updatePlayerPose", at = @At("HEAD"), cancellable = true)
    private void forcePose(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        IForm curForm = FormTextureUtils.getPlayerForm_Render(player);
        if (FormUtils.LockPoseToStand.hasFlag(curForm)) {
            this.setPose(EntityPose.STANDING);
            ci.cancel();
        }
        boolean isFeral = curForm.getBodyType() == PlayerFormBodyType.FERAL;
        if(isFeral){
            if (this.wouldNotSuffocateAtTargetPose(Pose.SWIMMING)) {
                Pose entityPose;
                if (this.isFallFlying()) {
                    entityPose = Pose.FALL_FLYING;
                } else if (this.isSleeping()) {
                    entityPose = Pose.STANDING;
                } else if (this.isSwimming()) {
                    entityPose = Pose.STANDING;
                } else if (this.isAutoSpinAttack()) {
                    entityPose = Pose.SPIN_ATTACK;
                } else if (this.isShiftKeyDown()) {
                    entityPose = Pose.CROUCHING;
                } else {
                    entityPose = Pose.STANDING;
                }

                Pose entityPose2;
                if (!this.isSpectator() && !this.isPassenger() && !this.wouldNotSuffocateAtTargetPose(entityPose)) {
                    if (this.wouldNotSuffocateAtTargetPose(Pose.CROUCHING)) {
                        entityPose2 = Pose.CROUCHING;
                    } else {
                        entityPose2 = Pose.STANDING;
                    }
                } else {
                    entityPose2 = entityPose;
                }

                this.setPose(entityPose2);
            }
            ci.cancel();

            /*if(isSwimming()){
                this.setPose(EntityPose.STANDING);
            }
            if(isSleeping()){
                this.setPose(EntityPose.STANDING);
            }*/
            //else if(this.isFallFlying()){
            //    this.setPose(EntityPose.STANDING);
            //}
            //ci.cancel();
        }
    }
}

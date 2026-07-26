package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.PlayerFormBodyType;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ElytraModel.class)
public abstract class ElytraEntityModelMixin<T extends LivingEntity> {
    @Shadow
    @Final
    private ModelPart rightWing;

    @Shadow
    @Final
    private ModelPart leftWing;

    @Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At("TAIL"),
            cancellable = true
    )
    private void customElytraAngles(T entity, float limbAngle, float limbDistance,
                                    float animationProgress, float headYaw, float headPitch,
                                    CallbackInfo ci) {
        float k = 0.2617994f;
        float l = -0.2617994f;
        float m = 0.0f;
        float n = 0.0f;
        if (((LivingEntity)entity).isFallFlying()) {
            float o = 1.0f;
            Vec3 vec3d = ((Entity)entity).getDeltaMovement();
            if (vec3d.y < 0.0) {
                Vec3 vec3d2 = vec3d.normalize();
                o = 1.0f - (float)Math.pow(-vec3d2.y, 1.5);
            }
            k = o * 0.34906584f + (1.0f - o) * k;
            l = o * -1.5707964f + (1.0f - o) * l;
        } else if (((Entity)entity).isCrouching()) {
            k = 0.6981317f;
            l = -0.7853982f;
            m = 3.0f;
            n = 0.08726646f;
        }
        // 特殊处理 BAT3的鞘翅轴心要向下移动来适配动画
        if (entity instanceof AbstractClientPlayer player) {
            IForm curForm0 = FormTextureUtils.getPlayerForm_Render(player);
            if(curForm0 == RegPlayerForms.BAT_3){
                //ShapeShifterCurseFabric.LOGGER.info("BAT3 set elytra");
                if (((LivingEntity)entity).onGround()){
                    if (((Entity)entity).isCrouching()){
                        k += (float)Math.toRadians(30.0);
                        m = 12.0f;
                    }
                    else{
                        k += (float)Math.toRadians(120.0);
                        //l += (float)Math.toRadians(70.0);
                        n += (float)Math.toRadians(45.0);
                        m = 9.0f;
                    }
                }
                else{
                    m = 0.0f;
                }
            }
        }

        this.leftWing.y = m;
        if (entity instanceof AbstractClientPlayer) {
            AbstractClientPlayer abstractClientPlayerEntity = (AbstractClientPlayer)entity;
            abstractClientPlayerEntity.elytraRotX += (k - abstractClientPlayerEntity.elytraRotX) * 0.1f;
            abstractClientPlayerEntity.elytraRotY += (n - abstractClientPlayerEntity.elytraRotY) * 0.1f;
            abstractClientPlayerEntity.elytraRotZ += (l - abstractClientPlayerEntity.elytraRotZ) * 0.1f;

            IForm curForm = FormTextureUtils.getPlayerForm_Render(abstractClientPlayerEntity);
            boolean isFeral = curForm.getBodyType() == PlayerFormBodyType.FERAL;
            if(isFeral){
                this.leftWing.xRot = k + (float)Math.toRadians(70.0);
                this.leftWing.zRot = l+ (float)Math.toRadians(70.0);
                this.leftWing.yRot = n+ (float)Math.toRadians(45.0);
            }
            else{
                this.leftWing.xRot = abstractClientPlayerEntity.elytraRotX;
                this.leftWing.yRot = abstractClientPlayerEntity.elytraRotY;
                this.leftWing.zRot = abstractClientPlayerEntity.elytraRotZ;
            }
        } else {
            this.leftWing.xRot = k;
            this.leftWing.zRot = l;
            this.leftWing.yRot = n;
        }
        this.rightWing.yRot = -this.leftWing.yRot;
        this.rightWing.y = this.leftWing.y;
        this.rightWing.xRot = this.leftWing.xRot;
        this.rightWing.zRot = -this.leftWing.zRot;

        // 取消原版的角度设置
        ci.cancel();
    }
}

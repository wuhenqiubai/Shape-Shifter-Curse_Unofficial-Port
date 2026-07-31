package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.PlayerFormBodyType;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ElytraModel.class)
public abstract class ElytraEntityModelMixin {
    @Shadow
    @Final
    private ModelPart rightWing;

    @Shadow
    @Final
    private ModelPart leftWing;

    // 1.21.11 中 elytraRotX/Y/Z 已从实体移到渲染状态（HumanoidRenderState），且渲染状态每帧重建，
    // 这里用实例字段（每个 ElytraModel 对应一个玩家的渲染器）维持原有的平滑动画
    @Unique
    private float ssc$elytraRotX;
    @Unique
    private float ssc$elytraRotY;
    @Unique
    private float ssc$elytraRotZ;

    @Inject(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
            at = @At("TAIL"),
            cancellable = true
    )
    private void customElytraAngles(HumanoidRenderState humanoidRenderState, CallbackInfo ci) {
        float k = 0.2617994f;
        float l = -0.2617994f;
        float m = 0.0f;
        float n = 0.0f;

        // 1.21.11 渲染状态中不再直接携带实体，通过 AvatarRenderState 的实体 id 反查当前正在渲染的玩家
        AbstractClientPlayer player = null;
        if (humanoidRenderState instanceof AvatarRenderState avatarRenderState && Minecraft.getInstance().level != null) {
            Entity entity = Minecraft.getInstance().level.getEntity(avatarRenderState.id);
            if (entity instanceof AbstractClientPlayer abstractClientPlayer) {
                player = abstractClientPlayer;
            }
        }

        if (humanoidRenderState.isFallFlying) {
            float o = 1.0f;
            Vec3 vec3d = player != null ? player.getDeltaMovement() : Vec3.ZERO;
            if (vec3d.y < 0.0) {
                Vec3 vec3d2 = vec3d.normalize();
                o = 1.0f - (float)Math.pow(-vec3d2.y, 1.5);
            }
            k = o * 0.34906584f + (1.0f - o) * k;
            l = o * -1.5707964f + (1.0f - o) * l;
        } else if (humanoidRenderState.isCrouching) {
            k = 0.6981317f;
            l = -0.7853982f;
            m = 3.0f;
            n = 0.08726646f;
        }
        // 特殊处理 BAT3的鞘翅轴心要向下移动来适配动画
        if (player != null) {
            IForm curForm0 = FormTextureUtils.getPlayerForm_Render(player);
            if(curForm0 == RegPlayerForms.BAT_3){
                //ShapeShifterCurseFabric.LOGGER.info("BAT3 set elytra");
                // 1.21.11 渲染状态中没有 onGround，用「非滑翔」近似表示在地面上
                if (!humanoidRenderState.isFallFlying){
                    if (humanoidRenderState.isCrouching){
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
        if (player != null) {
            this.ssc$elytraRotX += (k - this.ssc$elytraRotX) * 0.1f;
            this.ssc$elytraRotY += (n - this.ssc$elytraRotY) * 0.1f;
            this.ssc$elytraRotZ += (l - this.ssc$elytraRotZ) * 0.1f;

            IForm curForm = FormTextureUtils.getPlayerForm_Render(player);
            boolean isFeral = curForm.getBodyType() == PlayerFormBodyType.FERAL;
            if(isFeral){
                this.leftWing.xRot = k + (float)Math.toRadians(70.0);
                this.leftWing.zRot = l+ (float)Math.toRadians(70.0);
                this.leftWing.yRot = n+ (float)Math.toRadians(45.0);
            }
            else{
                this.leftWing.xRot = this.ssc$elytraRotX;
                this.leftWing.yRot = this.ssc$elytraRotY;
                this.leftWing.zRot = this.ssc$elytraRotZ;
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

package net.onixary.shapeShifterCurseFabric.mixin;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.PlayerFormBodyType;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 取消feral的潜行下压
@Environment(EnvType.CLIENT) // 仅客户端渲染逻辑
@Mixin(AvatarRenderer.class)
public abstract class FeralPlayerEntityRendererMixin {

    /**
     * 取消潜行时的模型下压动画
     * 1.21.11: AvatarRenderer.getRenderOffset(AvatarRenderState)（参数由实体改为 RenderState），从 id 恢复实体
     */
    @Inject(method = "getRenderOffset(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)Lnet/minecraft/world/phys/Vec3;", at = @At("RETURN"), cancellable = true)
    private void cancelSneakOffset(AvatarRenderState avatarRenderState, CallbackInfoReturnable<Vec3> ci)
    {
        Entity entity = Minecraft.getInstance().level.getEntity(avatarRenderState.id);
        if(entity instanceof AbstractClientPlayer abstractClientPlayerEntity){
            IForm curForm = FormTextureUtils.getPlayerForm_Render(abstractClientPlayerEntity);
            boolean isFeral = curForm.getBodyType() == PlayerFormBodyType.FERAL;
            if(isFeral){
                ci.setReturnValue(Vec3.ZERO);
            }
        }
    }
}

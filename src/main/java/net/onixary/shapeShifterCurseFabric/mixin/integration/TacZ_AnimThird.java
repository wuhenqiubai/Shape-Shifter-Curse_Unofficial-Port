package net.onixary.shapeShifterCurseFabric.mixin.integration;

import com.tacz.guns.client.animation.third.InnerThirdPersonManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.PlayerFormBodyType;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InnerThirdPersonManager.class, remap = false)
public class TacZ_AnimThird {
    @Inject(method = "setRotationAnglesHead", at = @At("HEAD"), cancellable = true, remap = false)
    private static void setRotationAnglesHead(LivingEntity entityIn, ModelPart rightArm, ModelPart leftArm, ModelPart body, ModelPart head, float limbSwingAmount, CallbackInfo ci) {
        if (entityIn instanceof Player player && !Minecraft.getInstance().isPaused()) {
            IForm form = FormTextureUtils.getPlayerForm_Render(player);
            if (form.getBodyType() == PlayerFormBodyType.FERAL) {
                ci.cancel();
            }
        }
    }
}

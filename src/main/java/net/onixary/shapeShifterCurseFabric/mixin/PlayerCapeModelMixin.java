package net.onixary.shapeShifterCurseFabric.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerCapeModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.PlayerFormBodyType;
import net.onixary.shapeShifterCurseFabric.player_form.utils.ModifyCapeRender;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(PlayerCapeModel.class)
public class PlayerCapeModelMixin {

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("HEAD"))
    private void ssc$clampCapeXRotation(AvatarRenderState avatarRenderState, CallbackInfo ci) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Entity entity = level.getEntity(avatarRenderState.id);
        if (!(entity instanceof AbstractClientPlayer player)) {
            return;
        }
        if (!ssc$needModifyXRotationAngle(player)) {
            return;
        }
        float movable = avatarRenderState.capeLean / 2.0F + avatarRenderState.capeFlap;
        float clamped = Mth.clamp(movable, -10.0F, 35.0F);
        avatarRenderState.capeFlap += clamped - movable;
    }

    @Unique
    private boolean ssc$needModifyXRotationAngle(AbstractClientPlayer player) {
        IForm curForm = FormTextureUtils.getPlayerForm_Render(player);
        if (curForm instanceof ModifyCapeRender mcr) {
            return mcr.NeedModifyXRotationAngle();
        } else {
            return curForm.getBodyType() == PlayerFormBodyType.FERAL;
        }
    }
}

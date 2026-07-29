package net.onixary.shapeShifterCurseFabric.mixin.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.skin.RegPlayerSkinComponent;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerEntityMixin {
    @Unique
    private static final Identifier CUSTOM_SKIN = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "textures/entity/base_player/ssc_base_skin.png");

    @Inject(method = "getSkin", at = @At("HEAD"), cancellable = true, order = 1000)
    private void shape_shifter_curse$modifyPlayerSkin(CallbackInfoReturnable<Identifier> cir) {
        AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;
        if (!RegPlayerForms.ORIGINAL_BEFORE_ENABLE.isPlayerForm(player))
        {
            if (FormTextureUtils.useTempCustomSkinConfig && Minecraft.getInstance().player == player) {
                if (FormTextureUtils.tempCustomSkinConfigOverrider.keepOriginalSkin()) {
                    return;
                } else {
                    cir.setReturnValue(CUSTOM_SKIN);
                    return;
                }
            }
            if (!RegPlayerSkinComponent.SKIN_SETTINGS.get(player).shouldKeepOriginalSkin()) {
                cir.setReturnValue(CUSTOM_SKIN);
                return;
            }
        }
        return;
    }
}
package net.onixary.shapeShifterCurseFabric.mixin.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.skin.RegPlayerSkinComponent;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityMixin {
    @Unique
    private static final Identifier CUSTOM_SKIN_TEXTURE = Identifier.of(ShapeShifterCurseFabric.MOD_ID, "textures/entity/base_player/ssc_base_skin.png");
    @Unique
    private static final SkinTextures CUSTOM_SKIN = new SkinTextures(CUSTOM_SKIN_TEXTURE, null, null, null, SkinTextures.Model.WIDE, false);

    @Inject(method = "getSkinTextures", at = @At("HEAD"), cancellable = true, order = 1000)
    private void shape_shifter_curse$modifyPlayerSkin(CallbackInfoReturnable<SkinTextures> cir) {
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;
        if (!RegPlayerForms.ORIGINAL_BEFORE_ENABLE.isPlayerForm(player))
        {
            if (FormTextureUtils.useTempCustomSkinConfig && MinecraftClient.getInstance().player == player) {
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
    }
}

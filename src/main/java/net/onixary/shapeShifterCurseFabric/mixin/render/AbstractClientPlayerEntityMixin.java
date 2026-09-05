package net.onixary.shapeShifterCurseFabric.mixin.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
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
    private static final ResourceLocation CUSTOM_SKIN = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "textures/entity/base_player/ssc_base_skin.png");

    // 1.21.1: getSkin() 返回 PlayerSkin（record），不能用 CallbackInfoReturnable<ResourceLocation>（HEAD 注入会 ClassCastException）。
    // 改用 RETURN 注入 + 构造新 PlayerSkin 只替换 texture，保留 cape/elytra/model 等字段。
    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true, order = 1000)
    private void shape_shifter_curse$modifyPlayerSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;
        if (!RegPlayerForms.ORIGINAL_BEFORE_ENABLE.isPlayerForm(player))
        {
            if (FormTextureUtils.useTempCustomSkinConfig && Minecraft.getInstance().player == player) {
                if (FormTextureUtils.tempCustomSkinConfigOverrider.keepOriginalSkin()) {
                    return;
                } else {
                    cir.setReturnValue(withCustomSkin(cir.getReturnValue()));
                    return;
                }
            }
            if (!RegPlayerSkinComponent.SKIN_SETTINGS.get(player).shouldKeepOriginalSkin()) {
                cir.setReturnValue(withCustomSkin(cir.getReturnValue()));
                return;
            }
        }
        return;
    }

    @Unique
    private static PlayerSkin withCustomSkin(PlayerSkin skin) {
        return new PlayerSkin(CUSTOM_SKIN, skin.textureUrl(), skin.capeTexture(), skin.elytraTexture(), skin.model(), skin.secure());
    }
}

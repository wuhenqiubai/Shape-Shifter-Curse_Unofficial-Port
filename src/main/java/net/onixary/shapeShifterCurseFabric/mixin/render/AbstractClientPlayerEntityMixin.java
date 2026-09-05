package net.onixary.shapeShifterCurseFabric.mixin.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.skin.RegPlayerSkinComponent;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerEntityMixin {
    // 1.21.11: ClientAsset.ResourceTexture(id) 会自动加 "textures/" 前缀和 ".png" 后缀，
    // 这里必须传纯路径（entity/base_player/ssc_base_skin），否则变成 textures/textures/...png.png 加载失败
    @Unique
    private static final Identifier CUSTOM_SKIN = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "entity/base_player/ssc_base_skin");

    // 1.21.11: AbstractClientPlayer.getSkin() 返回类型从 Identifier 改为 PlayerSkin（record：body/cape/elytra/model/secure）。
    // 旧版 @Inject(HEAD) + CallbackInfoReturnable<Identifier> 会因返回类型不匹配抛 ClassCastException。
    // 改用 RETURN 注入 + PlayerSkin.with(Patch)，只替换 body 纹理，保留 cape/elytra/model。
    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void shape_shifter_curse$modifyPlayerSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;
        if (!RegPlayerForms.ORIGINAL_BEFORE_ENABLE.isPlayerForm(player))
        {
            if (FormTextureUtils.useTempCustomSkinConfig && Minecraft.getInstance().player == player) {
                if (FormTextureUtils.tempCustomSkinConfigOverrider.keepOriginalSkin()) {
                    return;
                } else {
                    cir.setReturnValue(cir.getReturnValue().with(new PlayerSkin.Patch(
                            Optional.of(new ClientAsset.ResourceTexture(CUSTOM_SKIN)),
                            Optional.empty(), Optional.empty(), Optional.empty())));
                    return;
                }
            }
            if (!RegPlayerSkinComponent.SKIN_SETTINGS.get(player).shouldKeepOriginalSkin()) {
                cir.setReturnValue(cir.getReturnValue().with(new PlayerSkin.Patch(
                        Optional.of(new ClientAsset.ResourceTexture(CUSTOM_SKIN)),
                        Optional.empty(), Optional.empty(), Optional.empty())));
            }
        }
    }
}

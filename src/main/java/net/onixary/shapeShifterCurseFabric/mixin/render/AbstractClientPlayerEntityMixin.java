package net.onixary.shapeShifterCurseFabric.mixin.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.skin.RegPlayerSkinComponent;
import net.onixary.shapeShifterCurseFabric.render.form_render.ICanGetLastPos;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerEntityMixin implements ICanGetLastPos {
    @Unique
    private static final Identifier CUSTOM_SKIN = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "entity/base_player/ssc_base_skin");

    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true, order = 1000)
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
        return;
    }

    @Unique
    private Vec3 lastPos = Vec3.ZERO;

    @Inject(method = "tick", at = @At("HEAD"))
    private void shape_shifter_curse$lastPos(CallbackInfo ci) {
        lastPos = ((AbstractClientPlayer) (Object) this).position();
    }

    @Override
    public Vec3 shape_shifter_curse_fabric$getLastPos() {
        return lastPos;
    }

    @Override
    public double shape_shifter_curse_fabric$getLastPosX() {
        return lastPos.x;
    }

    @Override
    public double shape_shifter_curse_fabric$getLastPosY() {
        return lastPos.y;
    }

    @Override
    public double shape_shifter_curse_fabric$getLastPosZ() {
        return lastPos.z;
    }

    @Unique
    private static PlayerSkin withCustomSkin(PlayerSkin skin) {
        // 1.21.11: PlayerSkin 变为 record，且不再有 textureUrl()/capeTexture()/elytraTexture() 访问器；
        // 改用 with(Patch) 局部替换 body（只换皮肤贴图，披风/鞘翅/模型沿用原值）。
        return skin.with(new PlayerSkin.Patch(
                Optional.of(new ClientAsset.ResourceTexture(CUSTOM_SKIN)),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()));
    }
}

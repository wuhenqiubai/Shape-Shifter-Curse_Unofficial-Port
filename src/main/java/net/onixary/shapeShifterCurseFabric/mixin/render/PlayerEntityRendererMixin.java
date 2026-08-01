package net.onixary.shapeShifterCurseFabric.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.additional_power.NoRenderArmPower;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.skin.RegPlayerSkinComponent;
import net.onixary.shapeShifterCurseFabric.player_form.utils.RegPlayerFormComponent;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormRenderFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AvatarRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, AvatarRenderState, PlayerModel> {
    public PlayerEntityRendererMixin(EntityRendererProvider.Context ctx, PlayerModel model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    // 挂载Feature
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/player/PlayerRenderer;addLayer(Lnet/minecraft/client/renderer/entity/layers/RenderLayer;)Z", ordinal = 0))
    public void onInit(EntityRendererProvider.Context ctx, boolean slim, CallbackInfo ci) {
        this.addLayer(new FormRenderFeature<>((AvatarRenderer) (Object) this));
    }

    // 第一人称 渲染
    @Unique
    private static final Identifier CUSTOM_SKIN = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "textures/entity/base_player/ssc_base_skin.png");

    @Unique
    private static AbstractClientPlayer getRenderHandPlayer() {
        return Minecraft.getInstance().player;
    }

    // 1.21.11 renderHand 参数：poseStack, submitNodeCollector, i, identifier, modelPart, bl
    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void shape_shifter_curse$RenderArm_HEAD(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, Identifier identifier, ModelPart modelPart, boolean bl, CallbackInfo ci) {
        AbstractClientPlayer player = getRenderHandPlayer();
        if (player == null) {return;}
        if (RegPlayerFormComponent.PLAYER_FORM.get(player).nowForm.equals(RegPlayerForms.ORIGINAL_BEFORE_ENABLE)) {return;}  // 仅当玩家激活Mod后才进行修改
        if (!ShapeShifterCurseFabric.clientConfig.ignoreNoRenderArmPower && PowerHolderComponent.hasPower(player, NoRenderArmPower.class)) {  // 不渲染手臂情况
            ci.cancel();
        }
    }

    // 在 vanilla 提交手臂模型前，根据形态设置手臂组件是否显示
    @Inject(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModelPart(Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IILnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V", shift = At.Shift.BEFORE))
    private void shape_shifter_curse$RenderArm_PartA(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, Identifier identifier, ModelPart modelPart, boolean bl, CallbackInfo ci) {
        AbstractClientPlayer player = getRenderHandPlayer();
        if (player == null) {return;}
        if (RegPlayerFormComponent.PLAYER_FORM.get(player).nowForm.equals(RegPlayerForms.ORIGINAL_BEFORE_ENABLE)) {return;}  // 仅当玩家激活Mod后才进行修改
        if (!ShapeShifterCurseFabric.clientConfig.enableFormModelOnVanillaFirstPersonRender) {return;}  // 仅当启用自定义第一人称渲染时才进行修改
        AvatarRenderer realThis = (AvatarRenderer) (Object) this;
        PlayerModel playerModel = (PlayerModel) realThis.getModel();
        boolean IsRenderRight = modelPart.equals(playerModel.rightArm);
        ModelPart sleeve = IsRenderRight ? playerModel.rightSleeve : playerModel.leftSleeve;
        FormRenderFeature.rFPM_PartA(realThis, player, modelPart, sleeve);
    }

    // vanilla 提交手臂模型后，渲染变身模型的手臂骨骼（单骨骼渲染）
    @Inject(method = "renderHand", at = @At("RETURN"))
    private void shape_shifter_curse$RenderArm_PartB(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, Identifier identifier, ModelPart modelPart, boolean bl, CallbackInfo ci) {
        AbstractClientPlayer player = getRenderHandPlayer();
        if (player == null) {return;}
        if (RegPlayerFormComponent.PLAYER_FORM.get(player).nowForm.equals(RegPlayerForms.ORIGINAL_BEFORE_ENABLE)) {return;}  // 仅当玩家激活Mod后才进行修改
        if (!ShapeShifterCurseFabric.clientConfig.enableFormModelOnVanillaFirstPersonRender) {return;}  // 仅当启用自定义第一人称渲染时才进行修改
        AvatarRenderer realThis = (AvatarRenderer) (Object) this;
        PlayerModel playerModel = (PlayerModel) realThis.getModel();
        boolean IsRenderRight = modelPart.equals(playerModel.rightArm);
        ModelPart sleeve = IsRenderRight ? playerModel.rightSleeve : playerModel.leftSleeve;
        FormRenderFeature.rFPM_PartB(realThis, poseStack, submitNodeCollector, i, player, modelPart, sleeve);
    }

    // 第三人称皮肤（1.21.11 getTextureLocation 接收 AvatarRenderState）
    @Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)Lnet/minecraft/resources/Identifier;", at = @At("HEAD"), cancellable = true)
    private void shape_shifter_curse$onGetTexture(AvatarRenderState avatarRenderState, CallbackInfoReturnable<Identifier> cir) {
        Entity entity = Minecraft.getInstance().level.getEntity(avatarRenderState.id);
        if (entity instanceof Player player) {
            if (!RegPlayerFormComponent.PLAYER_FORM.get(player).nowForm.equals(RegPlayerForms.ORIGINAL_BEFORE_ENABLE)) {
                boolean keepOriginalSkin = RegPlayerSkinComponent.SKIN_SETTINGS.get(player).shouldKeepOriginalSkin();
                if(!keepOriginalSkin){
                    cir.setReturnValue(CUSTOM_SKIN);
                    cir.cancel();
                }
            }
        }
    }
}

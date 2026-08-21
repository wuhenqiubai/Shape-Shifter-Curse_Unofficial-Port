package net.onixary.shapeShifterCurseFabric.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public PlayerEntityRendererMixin(EntityRendererProvider.Context ctx, PlayerModel<AbstractClientPlayer> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    // 挂载Feature
    // NeoForge/Connector 兼容：原注入 <init> 内 addLayer INVOKE（NeoForge 把 LivingEntityRenderer.addLayer 从 protected 改成 public，
    // 构造器里 this.addLayer(...) 调用点的字节码符号引用 owner 变化，注入点失效）。改 <init> RETURN（构造器末尾，所有 addLayer
    // 已执行，跨 Fabric/NeoForge 稳定）。
    @Inject(method = "<init>(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;Z)V", at = @At("RETURN"))
    public void onInit(EntityRendererProvider.Context ctx, boolean slim, CallbackInfo ci) {
        this.addLayer(new FormRenderFeature<>((PlayerRenderer) (Object) this));
    }

    // 第一人称 渲染
    @Unique
    private static final ResourceLocation CUSTOM_SKIN = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "textures/entity/base_player/ssc_base_skin.png");

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void shape_shifter_curse$RenderArm_HEAD(PoseStack matrices, MultiBufferSource vertexConsumers, int light, AbstractClientPlayer player, ModelPart arm, ModelPart sleeve, CallbackInfo ci) {
        if (RegPlayerFormComponent.PLAYER_FORM.get(player).nowForm.equals(RegPlayerForms.ORIGINAL_BEFORE_ENABLE)) {return;}  // 仅当玩家激活Mod后才进行修改
        if (!ShapeShifterCurseFabric.clientConfig.ignoreNoRenderArmPower && PowerHolderComponent.hasPower(player, NoRenderArmPower.class)) {  // 不渲染手臂情况
            ci.cancel();
        }
    }

    @Inject(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/player/PlayerRenderer;setModelProperties(Lnet/minecraft/client/player/AbstractClientPlayer;)V", shift = At.Shift.AFTER))
    private void shape_shifter_curse$RenderArm_setModelPose_AFTER(PoseStack matrices, MultiBufferSource vertexConsumers, int light, AbstractClientPlayer player, ModelPart arm, ModelPart sleeve, CallbackInfo ci) {
        // 渲染变身模型-根据模型设置修改手臂组件渲染
        if (RegPlayerFormComponent.PLAYER_FORM.get(player).nowForm.equals(RegPlayerForms.ORIGINAL_BEFORE_ENABLE)) {return;}  // 仅当玩家激活Mod后才进行修改
        if (!ShapeShifterCurseFabric.clientConfig.enableFormModelOnVanillaFirstPersonRender) {return;}  // 仅当启用自定义第一人称渲染时才进行修改
        PlayerRenderer realThis = (PlayerRenderer) (Object) this;
        FormRenderFeature.rFPM_PartA(realThis, matrices, vertexConsumers, light, player, arm, sleeve);
    }

    @Inject(method = "renderHand", at = @At("RETURN"))
    private void shape_shifter_curse$RenderArm_RETURN(PoseStack matrices, MultiBufferSource vertexConsumers, int light, AbstractClientPlayer player, ModelPart arm, ModelPart sleeve, CallbackInfo ci) {
        // 渲染变身模型
        if (RegPlayerFormComponent.PLAYER_FORM.get(player).nowForm.equals(RegPlayerForms.ORIGINAL_BEFORE_ENABLE)) {return;}  // 仅当玩家激活Mod后才进行修改
        if (!ShapeShifterCurseFabric.clientConfig.enableFormModelOnVanillaFirstPersonRender) {return;}  // 仅当启用自定义第一人称渲染时才进行修改
        PlayerRenderer realThis = (PlayerRenderer) (Object) this;
        FormRenderFeature.rFPM_PartB(realThis, matrices, vertexConsumers, light, player, arm, sleeve);
    }

    @Redirect(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;getSkin()Lnet/minecraft/client/resources/PlayerSkin;"))
    private PlayerSkin shape_shifter_curse$getSkinTextures(AbstractClientPlayer player) {
        if (!RegPlayerFormComponent.PLAYER_FORM.get(player).nowForm.equals(RegPlayerForms.ORIGINAL_BEFORE_ENABLE)) {
            if (!RegPlayerSkinComponent.SKIN_SETTINGS.get(player).shouldKeepOriginalSkin()) {
                return new PlayerSkin(CUSTOM_SKIN, null, null, null, PlayerSkin.Model.WIDE, false);
            }
        }
        return player.getSkin();
    }


    // 第三人称皮肤
    @Inject(method = "getTextureLocation(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/resources/ResourceLocation;", at = @At("HEAD"), cancellable = true)
    private void shape_shifter_curse$onGetTexture(Entity entity, CallbackInfoReturnable<ResourceLocation> cir) {
        if(entity instanceof Player player) {
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
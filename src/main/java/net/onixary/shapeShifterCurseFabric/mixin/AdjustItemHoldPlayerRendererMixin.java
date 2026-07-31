package net.onixary.shapeShifterCurseFabric.mixin;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.onixary.shapeShifterCurseFabric.features.ExtraItemFeatureRenderer;
import net.onixary.shapeShifterCurseFabric.features.MouthItemFeature;
import net.onixary.shapeShifterCurseFabric.render.tech.ThirdPersonExtraHandItemRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;



@Environment(EnvType.CLIENT)
@Mixin(
        value = AvatarRenderer.class,
        priority = 1000
)
public abstract class AdjustItemHoldPlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, AvatarRenderState, PlayerModel> {
    public AdjustItemHoldPlayerRendererMixin(EntityRendererProvider.Context ctx, PlayerModel model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Inject(method = "<init>*", at = @At("RETURN"))
    public void init(EntityRendererProvider.Context ctx, boolean slim, CallbackInfo ci) {
        this.addLayer(new MouthItemFeature<>(this, this.entityRenderDispatcher.getItemInHandRenderer()));
        // ThirdPersonExtraHandItemRender 仍保留旧的 LivingEntity 泛型（RenderLayer 在 1.21.11 已改为渲染状态泛型），
        // 此处用原始类型适配调用，待该渲染器迁移后此调用仍可编译
        this.addLayer(new ThirdPersonExtraHandItemRender((RenderLayerParent) (Object) this, this.entityRenderDispatcher.getItemInHandRenderer()));
        ItemInHandRenderer itemRenderer = this.entityRenderDispatcher.getItemInHandRenderer();
        this.addLayer(new ExtraItemFeatureRenderer<>(this, this.entityRenderDispatcher, itemRenderer));
    }
}
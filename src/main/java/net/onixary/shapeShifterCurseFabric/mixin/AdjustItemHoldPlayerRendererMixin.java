package net.onixary.shapeShifterCurseFabric.mixin;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.onixary.shapeShifterCurseFabric.features.ExtraItemFeatureRenderer;
import net.onixary.shapeShifterCurseFabric.features.MouthItemFeature;
import net.onixary.shapeShifterCurseFabric.render.tech.ThirdPersonExtraHandItemRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;



@Environment(EnvType.CLIENT)
@Mixin(
        value = PlayerRenderer.class,
        priority = 1000
)
public abstract class AdjustItemHoldPlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public AdjustItemHoldPlayerRendererMixin(EntityRendererProvider.Context ctx, PlayerModel<AbstractClientPlayer> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Inject(method = "<init>*", at = @At("RETURN"))
    public void init(EntityRendererProvider.Context ctx, boolean slim, CallbackInfo ci) {
        this.addLayer(new MouthItemFeature<>(this, this.entityRenderDispatcher.getItemInHandRenderer()));
        this.addLayer(new ThirdPersonExtraHandItemRender<>(this, this.entityRenderDispatcher.getItemInHandRenderer()));
        ItemRenderer itemRenderer = ((IEntityRenderDispatcherAccessor) this.entityRenderDispatcher).getItemRenderer();
        this.addLayer(new ExtraItemFeatureRenderer<>(this, this.entityRenderDispatcher, itemRenderer));
    }
}

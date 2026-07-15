package net.onixary.shapeShifterCurseFabric.render.form_render;

import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;


@SuppressWarnings("removal")
public class FormAnimatable implements GeoAnimatable {
    AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        /* No-op: animations are driven by IModelAnimationSystem, not GeckoLib controllers */
    }

    public PlayerEntity e;

    public void setPlayer(PlayerEntity e) {
        this.e = e;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object o) {
	    return MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true);
    }
}

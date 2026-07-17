package net.onixary.shapeShifterCurseFabric.render.form_render;

import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;


public class FormAnimatable implements GeoAnimatable {
    AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<FormAnimatable>(this, ShapeShifterCurseFabric.MOD_ID, animationState -> {
            animationState.setAnimation(RawAnimation.begin().then("idle", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }));
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

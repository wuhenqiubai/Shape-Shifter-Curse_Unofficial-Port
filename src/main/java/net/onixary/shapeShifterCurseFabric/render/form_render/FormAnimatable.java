package net.onixary.shapeShifterCurseFabric.render.form_render;


import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.object.PlayState;


public class FormAnimatable implements GeoAnimatable {
    AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<FormAnimatable>(animationState -> {
	        animationState.setAnimation(RawAnimation.begin().thenLoop("idle"));
	        return PlayState.CONTINUE;
        }));
    }

    public Player e;

    public void setPlayer(Player e) {
        this.e = e;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

}
package net.onixary.shapeShifterCurseFabric.render.form_render;


import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animation.*;


public class FormAnimatable implements GeoAnimatable {
    AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<FormAnimatable>(this, ShapeShifterCurseFabric.MOD_ID, animationState -> {
	        animationState.setAnimation(RawAnimation.begin().then("idle", Animation.LoopType.LOOP));
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

    @Override
    public double getTick(Object o) {
	    return Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
    }
}

package net.onixary.shapeShifterCurseFabric.render.tech;

import com.zigythebird.playeranimcore.animation.Animation;
import net.minecraft.client.Minecraft;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.object.LoopType;
import software.bernie.geckolib.animation.object.PlayState;

public class EmptyAnimatable implements GeoAnimatable {
    AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<EmptyAnimatable>(animationState -> {
            animationState.setAnimation(RawAnimation.begin().then("idle", (LoopType) Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object object) {
        return Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
    }
}
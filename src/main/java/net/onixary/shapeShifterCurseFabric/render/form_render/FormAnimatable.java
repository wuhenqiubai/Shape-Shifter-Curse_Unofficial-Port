package net.onixary.shapeShifterCurseFabric.render.form_render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimationState;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.IAnimSystemAccessor;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import org.jetbrains.annotations.Nullable;


public class FormAnimatable implements GeoReplacedEntity {
    AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    public PlayerEntity e;

    public void setPlayer(PlayerEntity e) {
        this.e = e;
    }

    @Override
    public EntityType<?> getReplacingEntityType() {
        return EntityType.PLAYER;
    }

    private @Nullable AnimationState getAnimState() {
        if (e instanceof IAnimSystemAccessor accessor) {
            return accessor.shape_shifter_curse$getAnimSystem().animationState;
        }
        return null;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        return; // TEST: disable all GeckoLib AnimationControllers, rely on Mixin→processAnimation only
        /*controllers.add(new AnimationController<>(this, "main", 1, state -> {
            AnimationState animState = getAnimState();
            if (animState != null && animState.currentBodyAnimId != null) {
                state.setAnimation(RawAnimation.begin().thenPlay(animState.currentBodyAnimId.getPath()));
                state.setControllerSpeed(animState.bodySpeed);
            }
            if (this.e != null && this.e.age % 20 == 0) {
                RawAnimation currentRaw = RawAnimation.begin().thenPlay(animState != null && animState.currentBodyAnimId != null ? animState.currentBodyAnimId.getPath() : "none");
//                ShapeShifterCurseFabric.LOGGER.info("[SSC-ANIM] controller.main: animId={} speed={} animTick={} isCurrent={} playerSet={}",
//                    animState != null ? animState.currentBodyAnimId : "null",
//                    animState != null ? animState.bodySpeed : 0f,
//                    state.getAnimationTick(),
//                    state.isCurrentAnimation(currentRaw),
//                    this.e != null);
            }
            return PlayState.CONTINUE;
        }));

        controllers.add(new AnimationController<>(this, "upper", 1, state -> {
            AnimationState animState = getAnimState();
            if (animState != null && animState.currentUpperAnimId != null) {
                state.setAnimation(RawAnimation.begin().thenPlay(animState.currentUpperAnimId.getPath()));
                state.setControllerSpeed(animState.upperSpeed);
            }
            return PlayState.CONTINUE;
        }));

        controllers.add(new AnimationController<>(this, "power", 1, state -> {
            AnimationState animState = getAnimState();
            if (animState != null && animState.currentPowerAnimId != null) {
                state.setAnimation(RawAnimation.begin().thenPlay(animState.currentPowerAnimId.getPath()));
                state.setControllerSpeed(animState.powerSpeed);
            }
            return PlayState.CONTINUE;
        }));*/
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

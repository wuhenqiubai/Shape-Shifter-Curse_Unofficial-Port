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

    private String lastMainAnim = null, lastUpperAnim = null, lastPowerAnim = null;

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 1, state -> {
            AnimationState animState = getAnimState();
            if (animState != null && animState.currentBodyAnimId != null) {
                String id = animState.currentBodyAnimId.getPath();
                if (!id.equals(lastMainAnim)) {
                    lastMainAnim = id;
                    state.setControllerSpeed(animState.bodySpeed);
                    if (e != null && e.age % 20 == 0) ShapeShifterCurseFabric.LOGGER.info("[SSC-AC-PRED] main SWITCH to " + id);
                    return state.setAndContinue(RawAnimation.begin().thenPlay(id));
                }
                if (e != null && e.age % 20 == 0) ShapeShifterCurseFabric.LOGGER.info("[SSC-AC-PRED] main KEEP  " + id + " tick=" + state.getAnimationTick());
            } else {
                lastMainAnim = null;
                return PlayState.STOP;
            }
            return PlayState.CONTINUE;
        }));

        controllers.add(new AnimationController<>(this, "upper", 1, state -> {
            AnimationState animState = getAnimState();
            if (animState != null && animState.currentUpperAnimId != null) {
                String id = animState.currentUpperAnimId.getPath();
                if (!id.equals(lastUpperAnim)) {
                    lastUpperAnim = id;
                    state.setControllerSpeed(animState.upperSpeed);
                    return state.setAndContinue(RawAnimation.begin().thenPlay(id));
                }
            } else {
                lastUpperAnim = null;
                return PlayState.STOP;
            }
            return PlayState.CONTINUE;
        }));

        controllers.add(new AnimationController<>(this, "power", 1, state -> {
            AnimationState animState = getAnimState();
            if (animState != null && animState.currentPowerAnimId != null) {
                String id = animState.currentPowerAnimId.getPath();
                if (!id.equals(lastPowerAnim)) {
                    lastPowerAnim = id;
                    state.setControllerSpeed(animState.powerSpeed);
                    return state.setAndContinue(RawAnimation.begin().thenPlay(id));
                }
            } else {
                lastPowerAnim = null;
                return PlayState.STOP;
            }
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object o) {
        return e != null ? e.age + MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true) : 0;
    }
}
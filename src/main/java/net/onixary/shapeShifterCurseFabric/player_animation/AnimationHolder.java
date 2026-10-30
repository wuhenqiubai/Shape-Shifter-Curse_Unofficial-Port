package net.onixary.shapeShifterCurseFabric.player_animation;

import com.zigythebird.playeranim.animation.PlayerAnimResources;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.easing.EasingType;
import net.minecraft.resources.Identifier;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimUtils;
import org.jetbrains.annotations.Nullable;

public class AnimationHolder {
    public static AnimationHolder EMPTY = new AnimationHolder((Animation) null, false);

    private float speed;
    private int fade;
    private boolean isEnabled;
    @Nullable private Animation animation;
    @Nullable private EasingType easingType;
    private boolean skipFade;

    public @Nullable Identifier animationID;

    public AnimationHolder(@Nullable Identifier animation_id, boolean isEnabled, float speed) {
        this(PlayerAnimResources.getAnimation(animation_id), isEnabled, speed, 5);
        this.animationID = animation_id;
    }

    public AnimationHolder(@Nullable Identifier animation_id, boolean isEnabled) {
        this(PlayerAnimResources.getAnimation(animation_id), isEnabled, 1.0f, 2);
        this.animationID = animation_id;
    }

    public AnimationHolder(@Nullable Identifier animation_id, boolean isEnabled, float speed, int fade) {
        this(PlayerAnimResources.getAnimation(animation_id), isEnabled, speed, fade);
        this.animationID = animation_id;
    }

    public AnimationHolder(@Nullable Animation animation, boolean isEnabled, float speed, int fade) {
        this.isEnabled = isEnabled;
        this.animation = animation;
        this.speed = speed;
        this.fade = fade;
        this.animationID = null;
    }

    public AnimationHolder(@Nullable Animation animation, boolean isEnabled) {
        this(animation, isEnabled, 1.0f, 2);
    }

    public AnimationHolder() { this.isEnabled = false; this.animation = null; }

    @Nullable
    public Animation getAnimation() {
        if (!isEnabled) return null;

        return animation;
    }

    public AnimationHolder setAnimation(Animation animation) {
        this.animation = animation;

        return this;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public AnimationHolder setEnabled(boolean condition) {
        this.isEnabled = condition;

        return this;
    }

    public float getSpeed() {
        return speed;
    }

    public AnimationHolder setSpeed(float speed) {
        this.speed = speed;

        return this;
    }

    public int getFade() {
        return fade;
    }

    public AnimationHolder setFade(int fade) {
        this.fade = fade;

        return this;
    }

    @Nullable
    public EasingType getEasingType() {
        return easingType;
    }

    public AnimationHolder setEasingType(EasingType easingType) {
        this.easingType = easingType;
        return this;
    }

    public boolean isSkipFade() {
        return skipFade;
    }

    public AnimationHolder setSkipFade(boolean s) {
        this.skipFade = s;
        return this;
    }
}
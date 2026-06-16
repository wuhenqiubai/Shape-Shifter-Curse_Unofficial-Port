package net.onixary.shapeShifterCurseFabric.player_animation;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Animation data holder. Stores the animation identifier and playback metadata
 * (speed, fade, easing) for the current frame. Replaces the PAL Animation wrapper.
 *
 * @see net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimUtils.AnimationHolderData
 */
public class AnimationHolder {
    public static AnimationHolder EMPTY = new AnimationHolder(null, false);

    private float speed;
    private int fade;
    private boolean isEnabled;
    /** Animation identifier for GeckoLib lookup */
    @Nullable public Identifier animID;
    /** Easing type name for GeckoLib controller configuration */
    @Nullable public String easingTypeName;
    private boolean skipFade;

    public AnimationHolder(Identifier animation_id, boolean isEnabled, float speed) {
        this.animID = animation_id;
        this.isEnabled = isEnabled;
        this.speed = speed;
        this.fade = 5;
    }

    public AnimationHolder(Identifier animation_id, boolean isEnabled) {
        this(animation_id, isEnabled, 1.0f, 2);
    }

    public AnimationHolder(Identifier animation_id, boolean isEnabled, float speed, int fade) {
        this.animID = animation_id;
        this.isEnabled = isEnabled;
        this.speed = speed;
        this.fade = fade;
    }

    public AnimationHolder() {
        this.isEnabled = false;
        this.animID = null;
    }

    public boolean isEnabled() { return isEnabled; }
    public AnimationHolder setEnabled(boolean condition) { this.isEnabled = condition; return this; }

    public float getSpeed() { return speed; }
    public AnimationHolder setSpeed(float speed) { this.speed = speed; return this; }

    public int getFade() { return fade; }
    public AnimationHolder setFade(int fade) { this.fade = fade; return this; }

    @Nullable
    public String getEasingTypeName() { return easingTypeName; }
    public AnimationHolder setEasingTypeName(String easingTypeName) { this.easingTypeName = easingTypeName; return this; }

    public boolean isSkipFade() { return skipFade; }
    public AnimationHolder setSkipFade(boolean s) { this.skipFade = s; return this; }
}

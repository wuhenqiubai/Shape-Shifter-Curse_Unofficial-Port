package net.onixary.shapeShifterCurseFabric.player_animation.v3;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Per-frame animation state written by AnimSystem (tick) and read by GeckoLib AnimationController (render).
 * Stores resolved animation references and playback metadata for the current frame.
 */
public class AnimationState {
    // Main body animation
    public @Nullable Identifier currentBodyAnimId;
    public float bodySpeed = 1.0f;
    public int bodyTransitionTicks = 2;

    // Upper body override (using item, attacking)
    public @Nullable Identifier currentUpperAnimId;
    public float upperSpeed = 1.0f;
    public int upperTransitionTicks = 0;

    // Power animation (triggered externally)
    public @Nullable Identifier currentPowerAnimId;
    public float powerSpeed = 1.0f;
    public int powerTransitionTicks = 0;

    // Easing type name (for mapping to GeckoLib EasingType)
    public @Nullable String easingTypeName;

    // Callback invoked when power animation completes
    public @Nullable Runnable onPowerAnimComplete;

    public void clearBody() {
        currentBodyAnimId = null;
        bodySpeed = 1.0f;
        bodyTransitionTicks = 2;
        easingTypeName = null;
    }

    public void clearUpper() {
        currentUpperAnimId = null;
        upperSpeed = 1.0f;
        upperTransitionTicks = 0;
    }

    public void clearPower() {
        currentPowerAnimId = null;
        powerSpeed = 1.0f;
        powerTransitionTicks = 0;
        onPowerAnimComplete = null;
    }

    public void clearAll() {
        clearBody();
        clearUpper();
        clearPower();
    }
}

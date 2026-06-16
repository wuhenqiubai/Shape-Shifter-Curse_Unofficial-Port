package net.onixary.shapeShifterCurseFabric.player_animation;

import com.zigythebird.playeranim.animation.PlayerAnimResources;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.easing.EasingType;
import net.minecraft.util.Identifier;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimUtils;
import org.jetbrains.annotations.Nullable;

/**
 * 动画持有者，包装 PAL {@link Animation} 并提供播放参数（速度、淡入时间、缓动类型）。
 * <p>
 * 通过 {@link AnimUtils.AnimationHolderData#build()} 创建，所有形态的动画配置最终转化为此类实例。
 * 支持跳过淡入（{@link #skipFade}）用于处理不兼容平滑过渡的动画（如欧拉角大角度变化）。
 * <p>
 * 注意：{@link #EMPTY} 为禁用状态的哨兵对象，调用 {@link #getAnimation()} 返回 null。
 *
 * @see net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimUtils.AnimationHolderData
 * @see com.zigythebird.playeranimcore.easing.EasingType
 */
public class AnimationHolder {
    public static AnimationHolder EMPTY = new AnimationHolder((Animation) null, false);

    private float speed;
    private int fade;
    private boolean isEnabled;
    @Nullable private Animation animation;
    @Nullable private EasingType easingType;
    private boolean skipFade;
    /** Animation identifier for GeckoLib bridge. Set when constructed with an Identifier. */
    @Nullable public Identifier animID;

    public AnimationHolder(Identifier animation_id, boolean isEnabled, float speed) {
        this(PlayerAnimResources.getAnimation(animation_id), isEnabled, speed, 5);
        this.animID = animation_id;
    }

    public AnimationHolder(Identifier animation_id, boolean isEnabled) {
        this(PlayerAnimResources.getAnimation(animation_id), isEnabled, 1.0f, 2);
        this.animID = animation_id;
    }

    public AnimationHolder(Identifier animation_id, boolean isEnabled, float speed, int fade) {
        this(PlayerAnimResources.getAnimation(animation_id), isEnabled, speed, fade);
        this.animID = animation_id;
    }

    public AnimationHolder(@Nullable Animation animation, boolean isEnabled, float speed, int fade) {
        this.isEnabled = isEnabled;
        this.animation = animation;
        this.speed = speed;
        this.fade = fade;
    }

    public AnimationHolder(@Nullable Animation animation, boolean isEnabled) {
        this(animation, isEnabled, 1.0f, 2);
    }

    public AnimationHolder() { this.isEnabled = false; this.animation = null; }

	/**
	 * 获取动画实例。
	 *
	 * @return 动画实例，如果 {@link #isEnabled} 为 false 则返回 null
	 */
    @Nullable
    public Animation getAnimation() {
        if (!isEnabled) return null;
        return animation;
    }

    public AnimationHolder setAnimation(Animation animation) {
        this.animation = animation;
        return this;
    }

	/**
	 * @return 此动画持有者是否启用（禁用时 {@link #getAnimation()} 返回 null）
	 */
    public boolean isEnabled() { return isEnabled; }
    public AnimationHolder setEnabled(boolean condition) { this.isEnabled = condition; return this;
    }

	/** @return 播放速度倍率 */
    public float getSpeed() { return speed; }
    public AnimationHolder setSpeed(float speed) { this.speed = speed; return this;
    }

	/** @return 淡入过渡时间（tick） */
    public int getFade() { return fade; }
    public AnimationHolder setFade(int fade) { this.fade = fade; return this;
    }

	/** @return 缓动类型，null 表示使用默认线性 */
    @Nullable
    public EasingType getEasingType() { return easingType; }
    public AnimationHolder setEasingType(EasingType easingType) { this.easingType = easingType; return this;
    }

	/** @return 是否跳过淡入过渡（用于不兼容平滑过渡的动画） */
    public boolean isSkipFade() { return skipFade; }
    public AnimationHolder setSkipFade(boolean s) { this.skipFade = s; return this; }
}

package net.onixary.shapeShifterCurseFabric.player_animation;

import com.zigythebird.playeranimcore.animation.layered.IAnimation;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractFadeModifier;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import com.zigythebird.playeranimcore.easing.EasingType;
import com.zigythebird.playeranimcore.enums.FadeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 最短弧淡入修饰器（替代 PAL 的 {@link AbstractFadeModifier#standardFadeIn(int, EasingType)}）。
 * <p>
 * PAL 默认按欧拉角逐分量线性混合（{@code bone.scale(1-a).add(copy2.scale(a))}）。
 * 当两个动画姿态的某轴欧拉角跨 ±180（例如 idle 0° → crawling +201.62°，等价于 -158.38°）
 * 时，LINEAR 混合会走长弧导致翻转。
 * <p>
 * 本类覆写 {@link #get3DTransform}：在旋转分量混合前，把"新姿态"的欧拉角 unwrap 到离
 * "旧姿态"最近的等价角（|Δ|≤180），使 LINEAR 混合走最短弧。位置/缩放值很小，不受 ±360 影响。
 * <p>
 * 只依赖外部 PAL（不改/不打包 PAL），在 SSCU 侧实现，避开 NeoForge 下对 PAL 类的 Mixin。
 */
// TODO：到时候写为 时序连续 unwrap
public class ShortestArcFadeModifier extends AbstractFadeModifier {

	private final EasingType easing;
	private final @Nullable Float easingVariable;

	public ShortestArcFadeModifier(int length, EasingType easing) {
		this(length, easing, null);
	}

	public ShortestArcFadeModifier(int length, @Nullable EasingType easing, @Nullable Float easingVariable) {
		super(length);
		this.easing = easing != null ? easing : EasingType.LINEAR;
		this.easingVariable = easingVariable;
	}

	@Override
	protected float getAlpha(String boneName, float progress) {
		return this.easing.buildTransformer(this.easingVariable).apply(progress);
	}

	@Override
	protected FadeType getFadeType() {
		return FadeType.FADE_IN;
	}

	@Override
	public PlayerAnimBone get3DTransform(@NotNull PlayerAnimBone bone) {
		if (calculateProgress(tickDelta, bone.getName()) > 1) {
			IAnimation anim = getAnim();
			if (anim != null) anim.get3DTransform(bone);
			return bone;
		}

		PlayerAnimBone copy2 = new PlayerAnimBone(bone.getName());
		copy2.copyOtherBone(bone);
		// 取底层(新姿态)动画的姿态到 copy2；等价于基类里的 super.get3DTransform(copy2)，但不触发基类 blend。
		IAnimation anim = getAnim();
		if (anim != null) anim.get3DTransform(copy2);

		float a = getAlpha(copy2.getName(), calculateProgress(tickDelta, bone.getName()));
		if (getFadeType() == FadeType.FADE_IN) {
			if (transitionAnimation != null && transitionAnimation.isActive()) transitionAnimation.get3DTransform(bone);
		}

		// 最短弧：把"新姿态 copy2"的旋转逐个 unwrap 到靠近"旧姿态 bone"，使 LINEAR 混合走短路。
		copy2.rotX = unwrapToward(copy2.rotX, bone.rotX);
		copy2.rotY = unwrapToward(copy2.rotY, bone.rotY);
		copy2.rotZ = unwrapToward(copy2.rotZ, bone.rotZ);

		bone.scale(1 - a).add(copy2.scale(a));
		return bone;
	}

	/** 把 {@code value} 折算为离 {@code ref} 最近的等价角（弧度），保证 |value-ref| ≤ π。 */
	private static float unwrapToward(float value, float ref) {
		final float TWO_PI = (float) (2.0 * Math.PI);
		return value + Math.round((ref - value) / TWO_PI) * TWO_PI;
	}
}

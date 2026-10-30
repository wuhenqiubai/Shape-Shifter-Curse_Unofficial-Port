package net.onixary.shapeShifterCurseFabric.player_animation;

import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.layered.IAnimation;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractFadeModifier;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import com.zigythebird.playeranimcore.easing.EasingType;
import com.zigythebird.playeranimcore.enums.FadeType;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ShortestArcFadeModifier extends AbstractFadeModifier {

	private static final float VOTE_MIN_RAD = (float) Math.toRadians(90.0);

	private static final float FLIP_MIN_RAD = (float) Math.toRadians(150.0);

	private static final float TWO_PI = (float) (2.0 * Math.PI);

	private final EasingType easing;
	private final @Nullable Float easingVariable;

	private final Map<String, Map<String, Float>> nearestArcX = new HashMap<>();
	private final Map<String, Map<String, Float>> nearestArcY = new HashMap<>();
	private final Map<String, Map<String, Float>> nearestArcZ = new HashMap<>();

	private static final boolean DEBUG_FADE_LOG = false;

	private static final AtomicInteger DBG_FADE_SEQ = new AtomicInteger();

	private final int dbgFadeIndex = DBG_FADE_SEQ.incrementAndGet();

	private final Map<String, Integer> dbgLastLoggedTime = new HashMap<>();

	private final Map<String, Float> dbgLastLoggedTarget = new HashMap<>();

	private final Map<String, Integer> dbgCallCount = new HashMap<>();

	public ShortestArcFadeModifier(int length, EasingType easing) {
		this(length, easing, null);
	}

	public ShortestArcFadeModifier(int length, @Nullable EasingType easing, @Nullable Float easingVariable) {
		super(length);
		this.easing = easing != null ? easing : EasingType.LINEAR;
		this.easingVariable = easingVariable;
		if (DEBUG_FADE_LOG) {
			ShapeShifterCurseFabric.LOGGER.info("[SSC-FADE] ======== #{} fade start: length={} easing={} ========", dbgFadeIndex, length, this.easing);
		}
	}

	@Override
	protected float getAlpha(String boneName, float progress) {
		return this.easing.buildTransformer(this.easingVariable).apply(progress);
	}

	@Override
	protected FadeType getFadeType() {
		return FadeType.FADE_IN;
	}

	// PAL 1.2.6: IAnimation#get3DTransform(PlayerAnimBone) 返回 void（原地修改）——
	// 旧版是 PlayerAnimBone，所以此处不能再返回 bone。
	@Override
	public void get3DTransform(@NotNull PlayerAnimBone bone) {
		if (calculateProgress(tickDelta, bone.getName()) > 1) {
			IAnimation anim = getAnim();
			if (anim != null) anim.get3DTransform(bone);
			return;
		}

		PlayerAnimBone copy2 = new PlayerAnimBone(bone.getName());
		copy2.copyOtherBone(bone);
		IAnimation anim = getAnim();
		if (anim != null) anim.get3DTransform(copy2);

		float a = getAlpha(copy2.getName(), calculateProgress(tickDelta, bone.getName()));
		if (getFadeType() == FadeType.FADE_IN) {
			if (transitionAnimation != null && transitionAnimation.isActive()) transitionAnimation.get3DTransform(bone);
		}

		// 最短弧：把"新姿态 copy2"的旋转逐个 unwrap 到离"旧姿态 bone"最近的等价角，使 LINEAR 混合走短路。
		//
		// ⚠ 曾经试过把参考点改成「上一帧输出」（想做时序连续），实测**有害，已回退**：
		// 日志显示 get3DTransform 在一次渲染里会被多个 pass 调用多次（calls 能到 20），
		// 「上一帧输出」实际会被同一帧内的多次调用互相覆盖，参考点在帧内乱跳，
		// 反而让这个「离 ref 最近」的换支判定在淡入中途翻转（t=0 选 -170，t=1 选 +192）。
		// 保持原样：参考点 = 旧姿态（PAL 的 AnimationSnapshot，整轮淡入内是冻结的定值）。
		// PAL 1.2.6（26.1）：骨骼变换字段由扁平的 rotX/rotY/rotZ 改成了 JOML Vector3f，
		// 统一走 rotation.x/.y/.z。读用 .x 或 .x() 等价，但**写入只能用字段形式**
		// （.x() 返回 float 值不是可赋值位置；JOML 的 x(float) 是返回 Vector3f 的链式 setter）。
		float refX = bone.rotation.x;
		float refY = bone.rotation.y;
		float refZ = bone.rotation.z;

		float rawNewX = copy2.rotation.x;
		float rawNewY = copy2.rotation.y;
		float rawNewZ = copy2.rotation.z;

		String boneName = bone.getName();
		if (boneName == null) {
			// 理论上不会有匿名骨骼；保险起见退回"各取最近弧"
			copy2.rotation.x = unwrapToward(rawNewX, refX);
			copy2.rotation.y = unwrapToward(rawNewY, refY);
			copy2.rotation.z = unwrapToward(rawNewZ, refZ);
		} else {
			copy2.rotation.x = resolveTarget(nearestArcX, boneName, rawNewX, refX);
			copy2.rotation.y = resolveTarget(nearestArcY, boneName, rawNewY, refY);
			copy2.rotation.z = resolveTarget(nearestArcZ, boneName, rawNewZ, refZ);
		}

		// ⚠ 必须在下一行 scale(a) 之前取：copy2.scale(a) 是**原地**乘法，执行后 copy2 就变成 a×target 了
		float targetX = copy2.rotation.x;
		float targetY = copy2.rotation.y;
		float targetZ = copy2.rotation.z;

		bone.scale(1 - a).add(copy2.scale(a));

		if (DEBUG_FADE_LOG && bone.getName() != null && bone.getName().toLowerCase().contains("arm")) {
			dbgCallCount.merge(bone.getName(), 1, Integer::sum);
			Integer lastLoggedTime = dbgLastLoggedTime.get(bone.getName());
			Float lastLoggedTarget = dbgLastLoggedTarget.get(bone.getName());
			boolean timeChanged = lastLoggedTime == null || lastLoggedTime != time;
			boolean targetChanged = lastLoggedTarget == null || Math.abs(lastLoggedTarget - targetX) > 1.0e-4f;
			if (timeChanged || targetChanged) {
				dbgLastLoggedTime.put(bone.getName(), time);
				dbgLastLoggedTarget.put(bone.getName(), targetX);

				String animName = "?";
				AnimationController controller = getController();
				if (controller != null && controller.getCurrentAnimation() != null) {
					animName = controller.getCurrentAnimation().animation().getNameOrId();
				}

				ShapeShifterCurseFabric.LOGGER.info(
					"[SSC-FADE] #{} anim={} bone={} prog={} a={} time={} calls={} | refX={} rawNewX={} targetX={} dArcX={} dArcY={} dArcZ={} outX={}",
					dbgFadeIndex, animName, bone.getName(),
					calc(calculateProgress(tickDelta, bone.getName())), calc(a), time, dbgCallCount.get(bone.getName()),
					deg(refX), deg(rawNewX), deg(targetX), deg(targetX - refX),
					deg(targetY - refY), deg(targetZ - refZ), deg(bone.rotation.x));
			}
		}
	}

	private static double deg(float radians) {
		return Math.round(Math.toDegrees(radians) * 100.0) / 100.0;
	}

	private static double calc(float value) {
		return Math.round(value * 1000.0) / 1000.0;
	}

	private static String symmetryGroup(String boneName) {
		return boneName.toLowerCase().replace("left", "").replace("right", "");
	}

	private float resolveTarget(Map<String, Map<String, Float>> arcsByGroup, String boneName, float rawNew, float ref) {
		float d = unwrapToward(rawNew, ref) - ref;
		Map<String, Float> arcs = arcsByGroup.computeIfAbsent(symmetryGroup(boneName), k -> new HashMap<>());
		arcs.put(boneName, d);

		float costPositive = 0f;
		float costNegative = 0f;
		int voters = 0;
		for (float other : arcs.values()) {
			if (Math.abs(other) < VOTE_MIN_RAD) {
				continue;
			}
			voters++;
			costPositive += Math.abs(other >= 0 ? other : other + TWO_PI);
			costNegative += Math.abs(other <= 0 ? other : other - TWO_PI);
		}
		if (voters == 0) {
			return ref + d;
		}
		boolean preferPositive = costPositive < costNegative;

		if (Math.abs(d) >= FLIP_MIN_RAD) {
			if (preferPositive) {
				if (d < 0) d += TWO_PI;
			} else {
				if (d > 0) d -= TWO_PI;
			}
		}
		return ref + d;
	}

	private static float unwrapToward(float value, float ref) {
		float diff = (value - ref) % TWO_PI;
		if (diff > Math.PI) {
			diff -= TWO_PI;
		} else if (diff < -Math.PI) {
			diff += TWO_PI;
		}
		return ref + diff;
	}
}

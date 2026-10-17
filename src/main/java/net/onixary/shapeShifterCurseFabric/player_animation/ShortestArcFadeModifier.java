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

	@Override
	public PlayerAnimBone get3DTransform(@NotNull PlayerAnimBone bone) {
		if (calculateProgress(tickDelta, bone.getName()) > 1) {
			IAnimation anim = getAnim();
			if (anim != null) anim.get3DTransform(bone);
			return bone;
		}

		PlayerAnimBone copy2 = new PlayerAnimBone(bone.getName());
		copy2.copyOtherBone(bone);
		IAnimation anim = getAnim();
		if (anim != null) anim.get3DTransform(copy2);

		float a = getAlpha(copy2.getName(), calculateProgress(tickDelta, bone.getName()));
		if (getFadeType() == FadeType.FADE_IN) {
			if (transitionAnimation != null && transitionAnimation.isActive()) transitionAnimation.get3DTransform(bone);
		}

		float refX = bone.rotX;
		float refY = bone.rotY;
		float refZ = bone.rotZ;

		float rawNewX = copy2.rotX;
		float rawNewY = copy2.rotY;
		float rawNewZ = copy2.rotZ;

		String boneName = bone.getName();
		if (boneName == null) {
			copy2.rotX = unwrapToward(rawNewX, refX);
			copy2.rotY = unwrapToward(rawNewY, refY);
			copy2.rotZ = unwrapToward(rawNewZ, refZ);
		} else {
			copy2.rotX = resolveTarget(nearestArcX, boneName, rawNewX, refX);
			copy2.rotY = resolveTarget(nearestArcY, boneName, rawNewY, refY);
			copy2.rotZ = resolveTarget(nearestArcZ, boneName, rawNewZ, refZ);
		}

		float targetX = copy2.rotX;
		float targetY = copy2.rotY;
		float targetZ = copy2.rotZ;

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
					deg(targetY - refY), deg(targetZ - refZ), deg(bone.rotX));
			}
		}

		return bone;
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

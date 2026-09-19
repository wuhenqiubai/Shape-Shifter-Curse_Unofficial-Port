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

/**
 * 最短弧淡入修饰器（替代 PAL 的 {@link AbstractFadeModifier#standardFadeIn(int, EasingType)}）。
 * <p>
 * PAL 默认按欧拉角逐分量线性混合（{@code bone.scale(1-a).add(copy2.scale(a))}）。
 * 当两个动画姿态的某轴欧拉角跨 ±180（例如 idle 0° → crawling +201.62°，等价于 -158.38°）
 * 时，LINEAR 混合会走长弧导致翻转。
 * <p>
 * 本类覆写 {@link #get3DTransform}：在旋转分量混合前，把"新姿态"的欧拉角 unwrap 到最近的等价角
 * （|Δ|≤180），使 LINEAR 混合走最短弧。位置/缩放值很小，不受 ±360 影响。
 * <p>
 * 参考点取<b>旧姿态</b>：PAL 在切换瞬间给 fade 拍的是<b>静态快照</b>
 * （{@code AnimationController.replaceAnimationWithFade} 把 activeBones 包成 {@code AnimationSnapshot}），
 * 所以淡入期间"旧姿态"是整轮不动的定值。
 * <p>
 * <b>方向统一（本类的核心规则）</b>：单纯「每根骨骼各取离自己旧姿态最近的弧」是不够的 ——
 * 那会让本该同向运动的骨骼各走各的。典型是 `axolotl_3` 的 walk ↔ crawling：
 * 步行动画里两臂<b>反相摆动</b>（右 ≈ ±30°、左 ≈ ∓35°），爬行姿态里两臂<b>同相</b>（均 ≡ -165° 左右）。
 * 逐骨骼取最近弧时，右臂的 Δ 落在 -144°（清晰），左臂的 Δ 落在
 * <b>+173° vs -187°（跨在 ±180° 上，本来就模糊）</b>，于是两臂一个往前、一个往后转。
 * <p>
 * 因此这里在整轮淡入内<b>统一一个旋转方向</b>：
 * <ol>
 *   <li>每根骨骼/每个轴各自算出最近弧 {@code d}（{@code (-180°, 180°]}），记进投票表；</li>
 *   <li>对 {@code |d| ≥ }{@link #VOTE_MIN_RAD} 的骨骼投票，取代价小的一侧作为统一方向
 *       （代价 = 该方向下所有投票者转角的绝对值之和）；</li>
 *   <li>只对 {@code |d| ≥ }{@link #FLIP_MIN_RAD} 的骨骼强制用这个方向 ——
 *       这些正是"跨在 ±180° 上、方向模糊"的骨骼；转幅小的骨骼维持自己的最近弧，
 *       免得把只转 90° 的骨骼硬掰成 270°。</li>
 * </ol>
 * 投票表在 {@code get3DTransform} 被反复调用的过程中增量累积
 * （实测一次渲染里本方法会被多个 pass 调用十几次到二十几次），
 * 所以不需要额外的"帧末"回调；且 {@code a≈0} 时输出 ≈ 旧姿态，早期票不全也看不出来。
 * <p>
 * ⚠ 试过的错误方案：把参考点换成「上一帧输出」做时序连续 —— <b>实测有害</b>。
 * 「上一帧输出」会被同一帧内的多次调用互相覆盖，参考点在帧内乱跳，
 * 反而让换支判定在淡入中途翻转。故已回退。
 * <p>
 * 只依赖外部 PAL（不改/不打包 PAL），在 SSCU 侧实现，避开 NeoForge 下对 PAL 类的 Mixin。
 */
public class ShortestArcFadeModifier extends AbstractFadeModifier {

	/** 参与方向投票的门槛：转幅小于此值的骨骼视为"基本没动"，不投票。经验值，可调。 */
	private static final float VOTE_MIN_RAD = (float) Math.toRadians(90.0);

	/** 会被强制统一方向的门槛：只有转幅 ≥ 此值（即跨在 ±180° 附近、方向本就模糊）的骨骼才服从投票结果。经验值，可调。 */
	private static final float FLIP_MIN_RAD = (float) Math.toRadians(150.0);

	private static final float TWO_PI = (float) (2.0 * Math.PI);

	private final EasingType easing;
	private final @Nullable Float easingVariable;

	/**
	 * rotX / rotY / rotZ 三个轴各自的投票表，供整轮淡入的方向统一用。
	 * <p>
	 * 外层键 = <b>左右对称分组</b>（见 {@link #symmetryGroup}），内层键 = 骨骼名，值 = 该骨骼的最近弧（弧度）。
	 * <p>
	 * ⚠ 必须<b>按对称分组</b>而不是全局投票：把全身骨骼放进一张表会被无关骨骼污染 ——
	 * 例如 idle → crawling_idle 时腿/身体也在大幅移动，只要其中一根的最近弧是正的且量级够大，
	 * 全局投票就会翻成正方向，把两臂一起掰到错的方向去（实测发生过）。
	 * 而「左臂与右臂同向、左腿与右腿同向」才是真正需要的约束；手臂和腿本来就该各转各的。
	 */
	private final Map<String, Map<String, Float>> nearestArcX = new HashMap<>();
	private final Map<String, Map<String, Float>> nearestArcY = new HashMap<>();
	private final Map<String, Map<String, Float>> nearestArcZ = new HashMap<>();

	// ===== 调试插桩（默认关闭）。动画再出类似问题时，把 DEBUG_FADE_LOG 置 true 复用即可 =====

	/**
	 * 淡入调试日志开关。<b>默认 false</b>。
	 * <p>
	 * 打开后，每次淡入会为手臂骨骼打印：当前切到哪个动画、参考点 ref、新姿态 rawNew、
	 * 实际选中的目标支 target、以及 {@code dArcX/Y/Z}（= target − ref，带符号的实际转角）。
	 * 判据很好用：同一 {@code #N} 同一 tick 下，{@code right_arm} 与 {@code left_arm} 的 {@code dArcX} 是否<b>同号</b>
	 * —— 反向就说明两臂在朝相反方向转。
	 * <p>
	 * 注意：{@code time=0} 那一行是新动画的预热帧，PAL 还没算出新姿态，值不可信，读日志以 {@code time≥1} 为准。
	 */
	private static final boolean DEBUG_FADE_LOG = false;

	/** 全局递增的切换序号，便于在日志里指认「第几次切换」。 */
	private static final AtomicInteger DBG_FADE_SEQ = new AtomicInteger();

	/** 本次淡入的切换序号。 */
	private final int dbgFadeIndex = DBG_FADE_SEQ.incrementAndGet();

	/** 每根骨骼「上一次已打印的 tick 编号」——渲染一帧里 {@code get3DTransform} 会被多个渲染 pass 调用多次，按 tick 去重。 */
	private final Map<String, Integer> dbgLastLoggedTime = new HashMap<>();

	/** 每根骨骼「上一次已打印的 targetX」——投票表增量累积，同一 tick 内结果可能被后续调用纠正，纠正了也要打出来。 */
	private final Map<String, Float> dbgLastLoggedTarget = new HashMap<>();

	/** 每根骨骼在本轮淡入里被调用的总次数，用于判断调用频率是否正常。 */
	private final Map<String, Integer> dbgCallCount = new HashMap<>();

	// ===== 临时调试字段结束 =====

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
		// 取底层(新姿态)动画的姿态到 copy2；等价于基类里的 super.get3DTransform(copy2)，但不触发基类 blend。
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
		float refX = bone.rotX;
		float refY = bone.rotY;
		float refZ = bone.rotZ;

		float rawNewX = copy2.rotX;
		float rawNewY = copy2.rotY;
		float rawNewZ = copy2.rotZ;

		String boneName = bone.getName();
		if (boneName == null) {
			// 理论上不会有匿名骨骼；保险起见退回"各取最近弧"
			copy2.rotX = unwrapToward(rawNewX, refX);
			copy2.rotY = unwrapToward(rawNewY, refY);
			copy2.rotZ = unwrapToward(rawNewZ, refZ);
		} else {
			copy2.rotX = resolveTarget(nearestArcX, boneName, rawNewX, refX);
			copy2.rotY = resolveTarget(nearestArcY, boneName, rawNewY, refY);
			copy2.rotZ = resolveTarget(nearestArcZ, boneName, rawNewZ, refZ);
		}

		// ⚠ 必须在下一行 scale(a) 之前取：copy2.scale(a) 是**原地**乘法，执行后 copy2 就变成 a×target 了
		float targetX = copy2.rotX;
		float targetY = copy2.rotY;
		float targetZ = copy2.rotZ;

		bone.scale(1 - a).add(copy2.scale(a));

		// 调试插桩，默认关闭（见 DEBUG_FADE_LOG）。只打手臂骨骼（大小写不敏感匹配 "arm"，
		// 覆盖 right_arm / rightArm / bipedRightArm 等写法），避免刷屏。
		// 角度统一换算成「度」，便于与 player_animations/axolotl_3_*.json 里的数值直接比对。
		//   ref    = 参考点（= 旧姿态）
		//   rawNew = 底层新动画的实时姿态（unwrap 之前）
		//   target = 统一方向后实际被混合到的目标支
		//   dArc   = target − ref，即这一轴实际要转过的角度（带符号）
		//   out    = 最终写回 bone 的结果
		if (DEBUG_FADE_LOG && bone.getName() != null && bone.getName().toLowerCase().contains("arm")) {
			dbgCallCount.merge(bone.getName(), 1, Integer::sum);
			Integer lastLoggedTime = dbgLastLoggedTime.get(bone.getName());
			Float lastLoggedTarget = dbgLastLoggedTarget.get(bone.getName());
			// tick 编号变了要打；同一 tick 内如果投票表累积后把结果纠正了（resolveTarget 是增量投票），也要打出来
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

	// ===== 调试插桩辅助（随 DEBUG_FADE_LOG 一起保留，默认不产生输出） =====

	/** 弧度 → 度，保留两位小数，便于与动画 JSON 的数值直接比对。 */
	private static double deg(float radians) {
		return Math.round(Math.toDegrees(radians) * 100.0) / 100.0;
	}

	/** 保留三位小数。 */
	private static double calc(float value) {
		return Math.round(value * 1000.0) / 1000.0;
	}

	// ===== 调试插桩辅助结束 =====

	/**
	 * 由骨骼名求「左右对称分组」键 —— 把名字里的 left / right 抹掉，剩下的就是分组键。
	 * <p>
	 * 例：{@code right_arm} 与 {@code left_arm} 同组；{@code bipedLeftHindLeg} 与 {@code bipedRightHindLeg} 同组；
	 * {@code head} / {@code body} 这种没有左右之分的自成一元组 —— 组内只有它自己，投票结果就是它自己的最近弧，
	 * 不会被别的骨骼影响（这也是安全兜底：方向模糊但孤立的骨骼维持原行为）。
	 */
	private static String symmetryGroup(String boneName) {
		return boneName.toLowerCase().replace("left", "").replace("right", "");
	}

	/**
	 * 把某个轴上的"新姿态"折算成**要混合到的目标角** —— 在"各取最近弧"的基础上，额外做整轮淡入的方向统一。
	 *
	 * @param arcsByGroup 该轴按对称分组组织的投票表（分组 → 骨骼名 → 最近弧），本方法会写入本次的 {@code d}
	 * @param boneName    当前骨骼名
	 * @param rawNew   新姿态在该轴上的原始角（弧度，未折算）
	 * @param ref      参考点：旧姿态在该轴上的角（弧度）。整轮淡入内是冻结的定值
	 * @return 要混合到的目标角（弧度）。与 {@code rawNew} 只差 360° 的整数倍
	 */
	private float resolveTarget(Map<String, Map<String, Float>> arcsByGroup, String boneName, float rawNew, float ref) {
		// 1) 本骨骼的最近弧，落在 (-180°, 180°]
		float d = unwrapToward(rawNew, ref) - ref;
		Map<String, Float> arcs = arcsByGroup.computeIfAbsent(symmetryGroup(boneName), k -> new HashMap<>());
		arcs.put(boneName, d);

		// 2) 投票：只在**同一对称分组内**统计真的在动的骨骼（|d| ≥ VOTE_MIN_RAD），取"总转角更小"的统一方向
		float costPositive = 0f;
		float costNegative = 0f;
		int voters = 0;
		for (float other : arcs.values()) {
			if (Math.abs(other) < VOTE_MIN_RAD) {
				continue;
			}
			voters++;
			// 统一走正方向时，本来为负的骨骼要绕 +360
			costPositive += Math.abs(other >= 0 ? other : other + TWO_PI);
			// 统一走负方向时，本来为正的骨骼要绕 -360
			costNegative += Math.abs(other <= 0 ? other : other - TWO_PI);
		}
		if (voters == 0) {
			return ref + d;
		}
		boolean preferPositive = costPositive < costNegative;

		// 3) 只把"方向本就模糊"的骨骼（|d| ≥ FLIP_MIN_RAD）掰到统一方向上
		if (Math.abs(d) >= FLIP_MIN_RAD) {
			if (preferPositive) {
				if (d < 0) d += TWO_PI;
			} else {
				if (d > 0) d -= TWO_PI;
			}
		}
		return ref + d;
	}

	/** 把 {@code value} 折算为离 {@code ref} 最近的等价角（弧度），保证 |value-ref| ≤ π。 */
	private static float unwrapToward(float value, float ref) {
		// 用 fmod + if 把 (value-ref) 折到最短弧邻域 (-π, π]。
		// 比 Math.round((ref-value)/2π)*2π 更鲁棒：round 在差值恰好 ±0.5 圈（|Δ|=π）时
		// 四舍五入方向不定，可能误选长弧一侧；fmod+if 用严格比较把边界确定地归到 +π 侧。
		float diff = (value - ref) % TWO_PI;
		if (diff > Math.PI) {
			diff -= TWO_PI;
		} else if (diff < -Math.PI) {
			diff += TWO_PI;
		}
		return ref + diff;
	}
}

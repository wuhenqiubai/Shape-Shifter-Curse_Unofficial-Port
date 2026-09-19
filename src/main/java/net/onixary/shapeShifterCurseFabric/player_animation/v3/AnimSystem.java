package net.onixary.shapeShifterCurseFabric.player_animation.v3;

import com.zigythebird.playeranim.accessors.IAnimatedAvatar;
import com.zigythebird.playeranim.animation.AvatarAnimManager;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import com.zigythebird.playeranimcore.enums.TransformType;
import com.zigythebird.playeranimcore.loading.UniversalAnimLoader;
import com.zigythebird.playeranimcore.math.Vec3f;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_animation.AnimationHolder;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimFSM.FSMUtils;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateController.TransformingController;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// 每个玩家的动画系统
public class AnimSystem {

	/**
	 * 所有形态 {@code extra_parts_map} 里出现过的「动画侧骨骼名」，**已按 PAL 的规则归一化**（见 {@link #normalizeAnimBoneName}）。
	 * <p>
	 * 由 {@code DefaultModelAnimationSystem.loadConfig} 填充，由 {@code PlayerEntityAnimOverrideMixin} 消费
	 * （在触发动画前逐名注册进 PlayerAnimationController）。
	 * <p>
	 * 为什么必须注册：PAL 的 {@code AnimationController.setupNewAnimation} 只会把「控制器注册表里有的名字」
	 * 放进 {@code activeBones}，而 {@code PlayerAnimationController.registerBones()} 硬编码只注册 11 根 vanilla 骨；
	 * 未进 {@code activeBones} 的骨骼在 {@code get3DTransformRaw} 里会被**静默返回零值**（不报错、不打日志）。
	 * 上游用 PlayerAnimator 时按动画原始名直查、无需注册，所以这个坑是换库换出来的。
	 */
	public static final Set<String> EXTRA_ANIM_BONES = ConcurrentHashMap.newKeySet();

	/**
	 * 把骨骼名规范化成 PAL 内部使用的形式。
	 * <p>
	 * PAL 加载动画时（{@code AnimationLoader.bakeBoneAnimations}）会用
	 * {@code UniversalAnimLoader.getCorrectPlayerBoneName} 把每个骨骼 key 转成 snake_case
	 * （正则 {@code ([A-Z])} → {@code _$1} 后转小写），例如 {@code bipedRightHindLeg} → {@code biped_right_hind_leg}。
	 * 查询侧必须用同一套规则，否则永远查不到。
	 * <p>
	 * 这里直接调 PAL 自己的函数而不是自己写一遍正则，保证与 PAL 内部规则永远一致。
	 */
	public static String normalizeAnimBoneName(String name) {
		return UniversalAnimLoader.getCorrectPlayerBoneName(name);
	}
	public static class AnimSystemData {
		public IForm playerForm;
		public boolean IsOnGround = true;
		public Vec3 LastPosition;
        public long ContinueSwingAnimCounter = 0;  // 持续增长使用long防止溢出 顺便可以不用做最大值判断
        public boolean IsWalking = false;
        public long ContinueIdleStayTickCounter = 0;  // 静止Idle持续tick数 用于Idle停留动画 20tick=1秒
        public Vec3 fakeVelocity = Vec3.ZERO;
        public double fallDistanceTemp;
        public double fallDistance = 0;
        public CompoundTag customData;  // 用于存储其他拓展Mod的数据 在本模组中不使用

        public AnimSystemData(Player player) {
            this.playerForm = RegPlayerForms.ORIGINAL_BEFORE_ENABLE;
            this.customData = new CompoundTag();
            this.LastPosition = player.position();
            this.fallDistanceTemp = LastPosition.y;
        }
    }
    public final Player player;  // 玩家实体 理论上如果当前玩家实体被卸载了 那么这个AnimSystem也应该被卸载

    public AnimSystemData data;

	public static final Identifier defaultAnimFSMID = AnimRegistries.FSM_ON_GROUND;

	public Identifier nowAnimFSMID = defaultAnimFSMID;

    public final List<AbstractAnimStateController> PreProcessControllers;

    public @Nullable Identifier nowPlayingPowerAnimationID = null;
	public @Nullable Animation nowPlayingPowerAnimation = null;
    public int NPPA_Length = -1;
	public int NPPA_NowTick = 0;

    public @NotNull AbstractAnimFSM getAnimFSM() {
        // 及时崩溃报错 省的找问题
        return Objects.requireNonNull(AnimRegistry.getAnimFSM(nowAnimFSMID));
    }

    public AnimSystem(Player player) {
        this.player = player;
        this.data = new AnimSystemData(player);
        this.PreProcessControllers = new ArrayList<>();
        this.initPreProcessControllers();
        this.registerAllPreProcessControllers();
    }

    public void registerAllPreProcessControllers() {
        for (AbstractAnimStateController controller : this.PreProcessControllers) {
            if (!controller.isRegistered(this.player, this.data)) {
                controller.registerAnim(this.player, this.data);
            }
        }
    }

    public void initPreProcessControllers() {
        this.PreProcessControllers.add(new TransformingController());
    }

	public @Nullable AnimationHolder getPreProcessAnimation() {
        for (AbstractAnimStateController controller : this.PreProcessControllers) {
            if (controller.isEnabled(this.player, this.data)) {
                return controller.getAnimation(this.player, this.data);
            }
        }
        return null;
    }

    public static boolean checkOnGroundSuper(Player player) {
        if (player.onGround()) {
            return true;
        }
        if (player.getAbilities().flying) {
            return false;
        }
        return !player.level().noCollision(player.getBoundingBox().move(0, -0.01, 0).setMaxY(player.getY()));
    }

    private void PreProcessAnimSystemData() {
        Vec3 nowPos = this.player.position();
        this.data.fakeVelocity = nowPos.subtract(this.data.LastPosition);
        this.data.playerForm = FormTextureUtils.getPlayerForm_Render(this.player);
        this.data.IsWalking = !this.data.LastPosition.equals(nowPos);
        if (this.player.swinging) {
            this.data.ContinueSwingAnimCounter ++;
        }
        else {
            this.data.ContinueSwingAnimCounter = 0;
        }
        this.data.IsOnGround = checkOnGroundSuper(this.player);
        if (this.data.IsOnGround) {
            this.data.fallDistanceTemp = nowPos.y;
        } else {
            this.data.fallDistanceTemp = Math.max(this.data.fallDistanceTemp, nowPos.y);
        }
        this.data.fallDistance = this.data.fallDistanceTemp - nowPos.y;
        this.data.ContinueIdleStayTickCounter = FSMUtils.IsIdleStayCondition(this.player, this.data) ? this.data.ContinueIdleStayTickCounter + 1 : 0;
        this.NPPA_Tick();
    }

    private void EndProcessAnimSystemData() {
        this.data.LastPosition = this.player.position();
    }

    private void NPPA_Tick() {
        if (this.player instanceof IPlayerAnimController iPlayerAnimController) {
            if (this.nowPlayingPowerAnimationID != null && this.NPPA_Length > 0) {
                this.NPPA_NowTick++;
                if (this.NPPA_NowTick >= this.NPPA_Length) {
                    iPlayerAnimController.shape_shifter_curse$animationDoneCallBack(this.nowPlayingPowerAnimationID);
                    this.NPPA_NowTick = 0;
                }
            }
        }
    }

    private void NPPA_SetAnimation(@NotNull Identifier animID, @Nullable AnimationHolder anim) {
        if (animID.equals(this.nowPlayingPowerAnimationID)) {
            return;
        }
        this.nowPlayingPowerAnimationID = animID;
        this.nowPlayingPowerAnimation = anim == null ? null : anim.getAnimation();
        if (nowPlayingPowerAnimation == null) {
            this.NPPA_Length = -1;
            this.NPPA_NowTick = 0;
            return;
        }
        int AnimLength = (int) this.nowPlayingPowerAnimation.length();
        float Speed = anim.getSpeed();
        if (Speed == 0) {
            this.NPPA_Length = -1;
            this.NPPA_NowTick = 0;
        }
        else {
            this.NPPA_Length = (int) (AnimLength / Speed);
            this.NPPA_NowTick = 0;
        }
    }

    private @Nullable Identifier getPowerAnimID() {
        if (this.player instanceof IPlayerAnimController iPlayerAnimController) {
            return iPlayerAnimController.shape_shifter_curse$getPowerAnimationID();
        } else {
            ShapeShifterCurseFabric.LOGGER.error("Player {} is not a IPlayerAnimController when get power anim ID in AnimSystem", this.player.getName());
        }
	    return null;
    }

    public @Nullable AnimationHolder getAnimation() {  // 每Game Tick(0.05s)调用一次 否则NPPA(nowPlayPowerAnimation)系统会出问题
        this.PreProcessAnimSystemData();
        @Nullable AnimationHolder anim = this.getPreProcessAnimation();
        if (anim == null) {
            @Nullable Identifier powerAnimID = this.getPowerAnimID();
            if (powerAnimID != null) {
                if (!this.data.playerForm.isPowerAnimRegistered(this.player, this.data)) {
                    this.data.playerForm.registerPowerAnim(this.player, this.data);
                }
                Tuple<Boolean, @Nullable AnimationHolder> result = this.data.playerForm.getPowerAnim(this.player, this.data, powerAnimID);
                if (result.getA()) {
                    return result.getB();
                }
                @Nullable AnimRegistry.PowerDefaultAnim resultPowerDefaultAnim = AnimRegistry.getPowerDefaultAnim(powerAnimID);
                if (resultPowerDefaultAnim == null) {
                    return null;
                }
                anim = resultPowerDefaultAnim.ANIM_SYSTEM_GET_CURRENT_ANIM(this.player, this.data);
                this.NPPA_SetAnimation(powerAnimID, anim);
            } else {
                Tuple<@Nullable Identifier, @NotNull Identifier> result = this.getAnimFSM().update(this.player, this.data);
                if (result.getA() != null) {
                    this.nowAnimFSMID = result.getA();
                }
                Identifier animStateControllerID = result.getB();
                AbstractAnimStateController animStateController = this.data.playerForm.getAnimStateController(this.player, this.data, animStateControllerID);
                if (animStateController == null) {
                    AnimRegistry.AnimState resultAnimState = Objects.requireNonNull(AnimRegistry.getAnimState(animStateControllerID));
                    animStateController = resultAnimState.defaultController;
                }
                if (!animStateController.isRegistered(this.player, this.data)) {
                    animStateController.registerAnim(this.player, this.data);
                }
                anim = animStateController.getAnimation(this.player, this.data);
            }
        }
	    this.EndProcessAnimSystemData();
	    return anim;
    }

	public static @NotNull Vec3f getPlayerBone3DTransform(Player player, @NotNull String boneName, @NotNull TransformType type, @NotNull Vec3f defaultValue) {
		if (!(player instanceof Avatar avatar) || !(avatar instanceof IAnimatedAvatar animatedAvatar))
			return defaultValue;
		AvatarAnimManager manager = animatedAvatar.playerAnimLib$getAnimManager();
		if (manager == null || !manager.isActive()) return defaultValue;
		// 必须用归一化后的名字查询：PAL 内部存储的骨骼名是 snake_case（见 normalizeAnimBoneName）。
		// 传原始 camelCase（如 extra_parts_map 里的 bipedRightHindLeg）会永远查不到，
		// 而 PAL 查不到时是**静默返回零值**，表现为「这根骨头完全没有动画」。
		PlayerAnimBone bone = new PlayerAnimBone(normalizeAnimBoneName(boneName));
		bone = manager.get3DTransform(bone);
		return switch (type) {
			case POSITION -> new Vec3f(bone.getPosX(), bone.getPosY(), bone.getPosZ());
			case ROTATION -> new Vec3f(bone.getRotX(), bone.getRotY(), bone.getRotZ());
			case SCALE -> new Vec3f(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
			default -> defaultValue;
		};
	}
}
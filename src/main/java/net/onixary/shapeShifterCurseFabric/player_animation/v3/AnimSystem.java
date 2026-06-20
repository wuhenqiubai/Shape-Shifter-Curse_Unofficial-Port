package net.onixary.shapeShifterCurseFabric.player_animation.v3;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_animation.AnimationHolder;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateController.TransformingController;
import net.onixary.shapeShifterCurseFabric.player_form.PlayerFormBase;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 每个玩家的动画系统实例。管理 FSM 状态转换、Power 动画播放及预处理控制器。
 * <p>
 * 数据流：FSM ({@link AbstractAnimFSM}) → 状态 ID → 状态控制器 ({@link AbstractAnimStateController}) → 动画数据 ({@link AnimationHolder})
 * <br>
 * 或：Power 动画 ID → Power 动画注册表 → 动画数据
 * <p>
 * 每个 {@link PlayerEntity} 拥有一个 AnimSystem 实例，由 Mixin 注入管理。
 *
 * @see AbstractAnimFSM
 * @see AbstractAnimStateController
 * @see AnimationHolder
 */
public class AnimSystem {
    /** 默认动画 FSM ID（地面状态 {@code on_ground}） */
    public static final Identifier defaultAnimFSMID = AnimRegistries.FSM_ON_GROUND;
	/**
	 * 所属玩家实体。当此玩家实体被卸载时，AnimSystem 也应被卸载。
	 */
	public final PlayerEntity player;
	/** 前置处理器列表。在 FSM/Power 动画之前执行，用于变形过渡等特殊效果。 */
	public final List<AbstractAnimStateController> PreProcessControllers;
	/**
	 * 当前帧的系统数据。每 Game Tick (0.05s) 由 {@link #getAnimation} 更新一次。
	 */
	public AnimSystemData data;
	/**
	 * 当前活动的动画 FSM ID
	 */
	public Identifier nowAnimFSMID = defaultAnimFSMID;
	/** 当前正在播放的 Power 动画 ID */
	public @Nullable Identifier nowPlayingPowerAnimationID = null;
	/** 当前 Power 动画的总时长（tick） */
	public int NPPA_Length = -1;
	/**
	 * 当前 Power 动画已播放的 tick 数
	 */
	public int NPPA_NowTick = 0;
	/**
	 * Per-frame animation state for GeckoLib AnimationControllers.
	 * Written by {@link #getAnimation()} during tick, read by {@link FormAnimatable}'s controllers during render.
	 */
	public final AnimationState animationState = new AnimationState();

	/**
	 * 获取当前的动画 FSM 实例。
     *
     * @return 当前 FSM 实例
     * @throws NullPointerException 如果 FSM ID 未注册
     */
    public @NotNull AbstractAnimFSM getAnimFSM() {
        // 及时崩溃报错 省的找问题
        return Objects.requireNonNull(AnimRegistry.getAnimFSM(nowAnimFSMID));
    }

    public AnimSystem(PlayerEntity player) {
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

    private void PreProcessAnimSystemData() {
        // this.data.playerForm = RegPlayerFormComponent.PLAYER_FORM.get(this.player).getCurrentForm();
        this.data.playerForm = FormTextureUtils.getPlayerForm_Render(this.player);
        this.data.IsWalking = !this.data.LastPosition.equals(this.player.getPos());
        if (this.player.getPos().getY() == this.data.LastPosition.getY()) {
            this.data.LastPosYChange ++;
        }
        else {
            this.data.LastPosYChange = 0;
        }
        if (this.player.handSwinging) {
            this.data.ContinueSwingAnimCounter ++;
        }
        else {
            this.data.ContinueSwingAnimCounter = 0;
        }
        this.data.IsOnGround = (player.isOnGround() || (!player.getAbilities().flying && this.data.LastPosYChange > 10));
        this.NPPA_Tick();
    }

    private void EndProcessAnimSystemData() {
        this.data.LastPosition = this.player.getPos();
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
        if (anim == null || !anim.isEnabled()) {
            this.NPPA_Length = -1;
            this.NPPA_NowTick = 0;
            return;
        }
        float Speed = anim.getSpeed();
        if (Speed == 0) {
            this.NPPA_Length = -1;
            this.NPPA_NowTick = 0;
        } else {
            this.NPPA_Length = (int) (20 / Speed);
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

	/**
	 * 获取当前帧应播放的动画。
	 * <p>
	 * 每 Game Tick (0.05s) 调用一次，数据流：
	 * <ol>
	 *   <li>前置处理（变形过渡等）</li>
	 *   <li>Power 动画优先判断</li>
	 *   <li>FSM 状态转换 + 状态控制器获取动画</li>
	 * </ol>
     *
     * @return 要播放的动画持有者，null 表示无动画
     */
    public @Nullable AnimationHolder getAnimation() {
        this.PreProcessAnimSystemData();
        @Nullable AnimationHolder anim = this.getPreProcessAnimation();
        if (anim == null) {
            @Nullable Identifier powerAnimID = this.getPowerAnimID();
            if (powerAnimID != null) {
                if (!this.data.playerForm.isPowerAnimRegistered(this.player, this.data)) {
                    this.data.playerForm.registerPowerAnim(this.player, this.data);
                }
                Pair<Boolean, @Nullable AnimationHolder> result = this.data.playerForm.getPowerAnim(this.player, this.data, powerAnimID);
                if (result.getLeft()) {
                    anim = result.getRight();
                } else {
                    @Nullable AnimRegistry.PowerDefaultAnim resultPowerDefaultAnim = AnimRegistry.getPowerDefaultAnim(powerAnimID);
                    if (resultPowerDefaultAnim == null) {
                        this.EndProcessAnimSystemData();
                        this.animationState.clearAll();
                        return null;
                    }
                    anim = resultPowerDefaultAnim.ANIM_SYSTEM_GET_CURRENT_ANIM(this.player, this.data);
                    this.NPPA_SetAnimation(powerAnimID, anim);
                }
            } else {
                Pair<@Nullable Identifier, @NotNull Identifier> result = this.getAnimFSM().update(this.player, this.data);
                if (result.getLeft() != null) {
                    this.nowAnimFSMID = result.getLeft();
                }
                Identifier animStateControllerID = result.getRight();
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
        // Write to AnimationState for GeckoLib bridge
        if (anim != null && anim.isEnabled() && anim.animID != null) {
            if (!anim.animID.equals(this.animationState.currentBodyAnimId)) {
                this.animationState.bodyAnimStartAge = this.player.age;
            }
            this.animationState.currentBodyAnimId = anim.animID;
            this.animationState.bodySpeed = anim.getSpeed();
            this.animationState.bodyTransitionTicks = anim.getFade();
            this.animationState.easingTypeName = anim.getEasingTypeName();
        } else {
            this.animationState.clearBody();
        }
        this.EndProcessAnimSystemData();
        return anim;
    }

	/**
	 * 获取上半身覆盖动画（使用物品/攻击时）。
	 * 返回 UPPER-ONLY 动画，与主 FSM 动画同时在 PAL 不同层播放。
	 */
	public @Nullable AnimationHolder getUpperBodyOverride() {
		this.data.HasUpperBodyOverride = false;
		this.animationState.clearUpper();
		if (this.getPreProcessAnimation() != null) return null;

		if (this.player.isUsingItem()) {
			AbstractAnimStateController controller = this.data.playerForm.getAnimStateController(this.player, this.data, AnimRegistries.ANIM_STATE_USE_ITEM);
			if (controller == null) {
				AnimRegistry.AnimState animState = AnimRegistry.getAnimState(AnimRegistries.ANIM_STATE_USE_ITEM);
				if (animState != null) controller = animState.defaultController;
			}
			if (controller != null && controller.isRegistered(this.player, this.data) && controller.isEnabled(this.player, this.data)) {
				@Nullable AnimationHolder anim = controller.getAnimation(this.player, this.data);
				if (anim != null) {
					this.data.HasUpperBodyOverride = true;
					this.animationState.currentUpperAnimId = anim.animID;
					this.animationState.upperSpeed = anim.getSpeed();
					return anim;
				}
			}
		}
		if (this.player.handSwinging) {
			AbstractAnimStateController controller = this.data.playerForm.getAnimStateController(this.player, this.data, AnimRegistries.ANIM_STATE_ATTACK);
			if (controller == null) {
				AnimRegistry.AnimState animState = AnimRegistry.getAnimState(AnimRegistries.ANIM_STATE_ATTACK);
				if (animState != null) controller = animState.defaultController;
			}
			if (controller != null && controller.isRegistered(this.player, this.data) && controller.isEnabled(this.player, this.data)) {
				@Nullable AnimationHolder anim = controller.getAnimation(this.player, this.data);
				if (anim != null) {
					this.data.HasUpperBodyOverride = true;
					this.animationState.currentUpperAnimId = anim.animID;
					this.animationState.upperSpeed = anim.getSpeed();
					return anim;
				}
			}
		}
		return null;
	}

	/**
	 * 动画系统上下文数据，每帧由 {@link #getAnimation} 更新。
	 */
    public static class AnimSystemData {
		/**
		 * 当前帧该玩家的形态
		 */
        public PlayerFormBase playerForm;
		/** 是否在地面上（含容错判断：长时间 Y 轴未变化也视为地面） */
        public boolean IsOnGround = true;
		/** 上一帧的玩家位置，用于计算移动状态 */
        public Vec3d LastPosition;
		/**
		 * 玩家 Y 轴位置未变化的持续 tick 数
		 */
		public long LastPosYChange = 0;
		/**
		 * 玩家挥动手臂的持续 tick 数
		 */
		public long ContinueSwingAnimCounter = 0;
		/**
		 * 是否在移动
		 */
		public boolean IsWalking = false;
		/** 自定义 NBT 数据，供其他拓展 Mod 使用。SSC 本身不使用。 */
        public NbtCompound customData;
		/** 此帧是否有上半身动画覆盖 */
		public boolean HasUpperBodyOverride = false;

        public AnimSystemData(PlayerEntity player) {
            this.playerForm = RegPlayerForms.ORIGINAL_BEFORE_ENABLE;
            this.customData = new NbtCompound();
	        this.LastPosition = player.getPos();
        }
	}
}

package net.onixary.shapeShifterCurseFabric.player_animation.v3;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import com.zigythebird.playeranimcore.enums.TransformType;
import com.zigythebird.playeranimcore.math.Vec3f;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_animation.AnimationHolder;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateController.TransformingController;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// 每个玩家的动画系统
public class AnimSystem {
	public static class AnimSystemData {
		public IForm playerForm;
		public boolean IsOnGround = true;
		public Vec3 LastPosition;
        public long ContinueSwingAnimCounter = 0;  // 持续增长使用long防止溢出 顺便可以不用做最大值判断
		public boolean IsWalking = false;
        public CompoundTag customData;  // 用于存储其他拓展Mod的数据 在本模组中不使用

		public AnimSystemData(Player player) {
			this.playerForm = RegPlayerForms.ORIGINAL_BEFORE_ENABLE;
			this.customData = new CompoundTag();
			this.LastPosition = player.position();
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
        this.data.playerForm = FormTextureUtils.getPlayerForm_Render(this.player);
        this.data.IsWalking = !this.data.LastPosition.equals(this.player.position());
        if (this.player.swinging) {
            this.data.ContinueSwingAnimCounter ++;
        }
        else {
            this.data.ContinueSwingAnimCounter = 0;
        }
        this.data.IsOnGround = checkOnGroundSuper(this.player);
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
		if (!(player instanceof AbstractClientPlayer clientPlayer) || !(clientPlayer instanceof IAnimatedPlayer animatedPlayer))
			return defaultValue;
		PlayerAnimManager manager = animatedPlayer.playerAnimLib$getAnimManager();
		if (manager == null || !manager.isActive()) return defaultValue;
		PlayerAnimBone bone = new PlayerAnimBone(boneName);
		bone = manager.get3DTransform(bone);
		return switch (type) {
			case POSITION -> new Vec3f(bone.getPosX(), bone.getPosY(), bone.getPosZ());
			case ROTATION -> new Vec3f(bone.getRotX(), bone.getRotY(), bone.getRotZ());
			case SCALE -> new Vec3f(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
			default -> defaultValue;
		};
	}
}
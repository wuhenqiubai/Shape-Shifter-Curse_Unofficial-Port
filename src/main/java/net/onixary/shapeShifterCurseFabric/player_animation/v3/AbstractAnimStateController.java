package net.onixary.shapeShifterCurseFabric.player_animation.v3;

import net.minecraft.entity.player.PlayerEntity;
import net.onixary.shapeShifterCurseFabric.player_animation.AnimationHolder;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import org.jetbrains.annotations.Nullable;

/**
 * 抽象动画状态控制器。
 * <p>
 * 每种动画状态（如 IDLE、SWIM、WALK）对应一个控制器实例。
 * 控制器负责在 {@link #getAnimation} 中根据当前玩家状态和动画系统数据返回合适的动画。
 * <p>
 * 生命周期：{@link #registerAnim} → 多次调用 {@link #getAnimation}。
 * 控制器通过 {@link #isEnabled} 控制是否生效（用于变形过渡等特殊场景）。
 *
 * @see AnimStateEnum
 * @see net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateControllerDP
 * @see IForm#getAnimStateController
 */
public abstract class AbstractAnimStateController {
	/**
	 * 获取当前应播放的动画。
	 *
	 * @param player 目标玩家
	 * @param data   动画系统数据（包含 FSM 上下文）
	 * @return 动画持有者，null 表示使用默认行为
	 */
    public abstract @Nullable AnimationHolder getAnimation(PlayerEntity player, AnimSystem.AnimSystemData data);

    private boolean isRegistered = false;

	/**
	 * 控制器是否已注册。
	 *
	 * @return 是否已调用过 {@link #registerAnim}
	 */
    public boolean isRegistered(PlayerEntity player, AnimSystem.AnimSystemData data) {
        return isRegistered;
    }

	/**
	 * 注册此控制器（标记可用）。在形态度量初始化时由 {@link AnimSystem} 调用。
	 */
    public void registerAnim(PlayerEntity player, AnimSystem.AnimSystemData data) {
        isRegistered = true;
    }

	/**
	 * 控制器是否启用。
	 * <p>
	 * 默认始终启用。仅在特殊系统（如 {@link net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateController.TransformingController} 变形过渡控制器）
	 * 中根据变形状态动态关闭。
	 *
	 * @return true 表示此控制器当前应生效
	 */
    public boolean isEnabled(PlayerEntity player, AnimSystem.AnimSystemData data) {
        return true;
    }
}

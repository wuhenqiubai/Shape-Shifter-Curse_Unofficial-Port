package net.onixary.shapeShifterCurseFabric.player_animation.v3;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 抽象动画有限状态机（FSM），驱动玩家动画状态的转换。
 * <p>
 * FSM 的工作方式：
 * <ol>
 *   <li>{@link AnimSystem} 每帧调用 {@link #update}</li>
 *   <li>{@link #getNextFSM} 确定是否要切换到下一个 FSM（返回 null 表示留在当前 FSM）</li>
 *   <li>{@link #getStateID} 返回当前 FSM 应使用的动画状态 ID</li>
 * </ol>
 * <p>
 * 支持链式转换：FSM A → FSM B → FSM C，由 {@link #update} 中的递归逻辑处理。
 * 最终输出的状态 ID 用于在 {@link IForm#getAnimStateController} 中查找对应的 {@link AbstractAnimStateController}。
 *
 * @see AnimRegistry#getAnimFSM
 * @see net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimFSM.InAirFSM
 * @see net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimFSM.OnGroundFSM
 */
public abstract class AbstractAnimFSM {
	/**
	 * 更新 FSM 状态，返回此次 update 的结果。
	 * <p>
	 * 返回值说明：
	 * <ul>
	 *   <li>Pair.left：跳转到的下一个 FSM ID（null 表示无跳转）</li>
	 *   <li>Pair.right：当前应使用的动画状态 ID</li>
	 * </ul>
	 * <p>
	 * 注意：此方法不推荐被子类覆写，子类应覆写 {@link #getNextFSM} 和 {@link #getStateID}。
	 *
	 * @param player         目标玩家
	 * @param animSystemData 动画系统数据
	 * @return left = 下一个 FSM ID（null=不切换），right = 动画状态 ID
	 */
    public Pair<@Nullable Identifier, @NotNull Identifier> update(PlayerEntity player, AnimSystem.AnimSystemData animSystemData) {
        Identifier NextFSMID = getNextFSM(player, animSystemData);
        if (NextFSMID == null) {
            return new Pair<>(null, getStateID(player, animSystemData));
        } else {
            AbstractAnimFSM NextFSM = AnimRegistry.getAnimFSM(NextFSMID);
            if (NextFSM == null) {
                ShapeShifterCurseFabric.LOGGER.error("找不到动画控制状态机: {}", NextFSMID);
                return new Pair<>(null, getStateID(player, animSystemData));
            }
            Pair<@Nullable Identifier, @NotNull Identifier> NextFSMResult = NextFSM.update(player, animSystemData);
            if (NextFSMResult.getLeft() != null) {  // 多次跳转
                return NextFSMResult;
            }
            else {  // 单次跳转
                return new Pair<>(NextFSMID, NextFSMResult.getRight());
            }
        }
    }

	/**
	 * 判断当前状态下是否需要切换到另一个 FSM。
	 * <p>
	 * 子类实现此方法，根据玩家状态（如在空中、在地面、使用物品等）决定是否切换 FSM。
	 *
	 * @param player         目标玩家
	 * @param animSystemData 动画系统数据
	 * @return 下一个 FSM 的 ID，返回 null 表示不切换（留在当前 FSM）
	 */
	public abstract @Nullable Identifier getNextFSM(PlayerEntity player, AnimSystem.AnimSystemData animSystemData);

	/**
	 * 获取当前 FSM 应使用的动画状态 ID。
	 * <p>
	 * 返回的 ID 应匹配 {@link AnimRegistries} 中注册的 ANIM_STATE_* 常量之一，
	 * 或者是一个自定义状态 ID。
	 *
	 * @param player         目标玩家
	 * @param animSystemData 动画系统数据
	 * @return 动画状态 ID
	 */
    public abstract @NotNull Identifier getStateID(PlayerEntity player, AnimSystem.AnimSystemData animSystemData);
}

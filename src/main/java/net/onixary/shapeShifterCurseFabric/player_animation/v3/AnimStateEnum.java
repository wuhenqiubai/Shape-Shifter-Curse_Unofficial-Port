package net.onixary.shapeShifterCurseFabric.player_animation.v3;

import net.minecraft.util.Identifier;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * 玩家动画状态枚举，快速映射 FSM 输出的状态 ID 到枚举常量。
 * <p>
 * 用于 {@link IForm#getAnimStateController} 中的 switch 语句，
 * 使 FSM 状态到动画控制器的映射代码更简洁。
 * <p>
 * 状态与 {@link AnimRegistries} 中注册的 ANIM_STATE_* Identifier 一一对应。
 *
 * @see AbstractAnimFSM#getStateID
 * @see net.onixary.shapeShifterCurseFabric.player_form.forms.Form_Axolotl2#getAnimStateController
 */
public enum AnimStateEnum {
	/**
	 * 睡觉
	 */
    ANIM_STATE_SLEEP,
	/** 骑乘 */
    ANIM_STATE_RIDE,
	/** 攀爬 */
    ANIM_STATE_CLIMB,
	/** 游泳 */
    ANIM_STATE_SWIM,
	/** 飞行（如蝙蝠） */
    ANIM_STATE_FLYING,
	/** 滑翔坠落 */
    ANIM_STATE_FALL_FLYING,
	/** 坠落 */
    ANIM_STATE_FALL,
	/** 跳跃 */
    ANIM_STATE_JUMP,
	/** 使用物品 */
    ANIM_STATE_USE_ITEM,
	/** 挖掘 */
    ANIM_STATE_MINING,
	/** 攻击 */
    ANIM_STATE_ATTACK,
	/** 行走 */
    ANIM_STATE_WALK,
	/** 冲刺 */
    ANIM_STATE_SPRINT,
	/** 待机 */
    ANIM_STATE_IDLE;

    public static final HashMap <Identifier, AnimStateEnum> stateMap = new HashMap<>();

    static {
        stateMap.put(AnimRegistries.ANIM_STATE_SLEEP, ANIM_STATE_SLEEP);
        stateMap.put(AnimRegistries.ANIM_STATE_RIDE, ANIM_STATE_RIDE);
        stateMap.put(AnimRegistries.ANIM_STATE_CLIMB, ANIM_STATE_CLIMB);
        stateMap.put(AnimRegistries.ANIM_STATE_SWIM, ANIM_STATE_SWIM);
        stateMap.put(AnimRegistries.ANIM_STATE_FLYING, ANIM_STATE_FLYING);
        stateMap.put(AnimRegistries.ANIM_STATE_FALL_FLYING, ANIM_STATE_FALL_FLYING);
        stateMap.put(AnimRegistries.ANIM_STATE_FALL, ANIM_STATE_FALL);
        stateMap.put(AnimRegistries.ANIM_STATE_JUMP, ANIM_STATE_JUMP);
        stateMap.put(AnimRegistries.ANIM_STATE_USE_ITEM, ANIM_STATE_USE_ITEM);
        stateMap.put(AnimRegistries.ANIM_STATE_MINING, ANIM_STATE_MINING);
        stateMap.put(AnimRegistries.ANIM_STATE_ATTACK, ANIM_STATE_ATTACK);
        stateMap.put(AnimRegistries.ANIM_STATE_WALK, ANIM_STATE_WALK);
        stateMap.put(AnimRegistries.ANIM_STATE_SPRINT, ANIM_STATE_SPRINT);
        stateMap.put(AnimRegistries.ANIM_STATE_IDLE, ANIM_STATE_IDLE);
    }

	/**
	 * 将 FSM 输出的状态 ID 转换为枚举常量。
	 *
	 * @param stateID 状态 ID，通常来自 {@link AnimRegistries} 的 ANIM_STATE_* 常量
	 * @return 对应的枚举常量，如果未识别则返回 null
     */
    public static @Nullable AnimStateEnum getStateEnum(Identifier stateID) {
        return stateMap.get(stateID);
    }
}

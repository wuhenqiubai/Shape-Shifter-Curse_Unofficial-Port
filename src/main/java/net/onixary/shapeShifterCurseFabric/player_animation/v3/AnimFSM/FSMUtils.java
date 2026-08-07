package net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimFSM;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AbstractAnimStateController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimRegistries;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateController.IdleStayAnimController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimSystem;
import org.jetbrains.annotations.Nullable;

import static net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimRegistries.*;

public class FSMUtils {
    public static @Nullable Identifier ProcessUniversalAnim(Player player, AnimSystem.AnimSystemData animSystemData) {
        if (player.isSleeping()) {
            return ANIM_STATE_SLEEP;
        }
        if (player.isPassenger()) {
            return ANIM_STATE_RIDE;
        }
        if (!animSystemData.IsOnGround && IsPlayerClimbing(player, animSystemData)) {
            return ANIM_STATE_CLIMB;
        }
        if (player.isInWater()) {
            return ANIM_STATE_SWIM;
        }
        return null;
    }

    public static @Nullable IdleStayAnimController GetIdleStayController(Player player, AnimSystem.AnimSystemData animSystemData) {
        AbstractAnimStateController idleController = animSystemData.playerForm.getAnimStateController(
                player,
                animSystemData,
                AnimRegistries.ANIM_STATE_IDLE
        );
        if (idleController instanceof IdleStayAnimController idleStayController) {
            return idleStayController;
        }
        return null;
    }

    // 判定玩家是否处于"静止Idle"状态(在地面且无任何动作) 用于Idle停留动画 潜行时不触发
    public static boolean IsIdleStayCondition(Player player, AnimSystem.AnimSystemData animSystemData) {
        // 只为实际配置了停留动画的形态计时，避免从其他形态继承已经累计的静止时间
        if (GetIdleStayController(player, animSystemData) == null) {
            return false;
        }
        if (!animSystemData.IsOnGround) {
            return false;
        }
        if (ProcessUniversalAnim(player, animSystemData) != null) {  // Sleep/Ride/Climb/Swim
            return false;
        }
        if (player.isVisuallyCrawling()) {
            return false;
        }
        if (player.isShiftKeyDown()) {  // 潜行时不触发停留动画
            return false;
        }
        if (animSystemData.IsWalking) {
            return false;
        }
        if (player.isUsingItem() || player.swinging) {
            return false;
        }
        return true;
    }

    // 祖传代码 从第1代修改攀爬条件时就开始使用了 从v2上复制的
    public static boolean IsPlayerClimbing(Player player, AnimSystem.AnimSystemData animSystemData) {
        if (!player.onClimbable() || player.onGround() || player.getAbilities().flying || player.isFallFlying()) {
            return false;
        }
        // 检测碰撞箱 防止出现身体与地面穿模 如果卡顿可以直接可以修改为 return true
        BlockPos down1pos = player.blockPosition().below();
        BlockState down1block = player.level().getBlockState(down1pos);
        Vec3 ClimbAnimTestPoint = player.position().add(0f, -0.6f, 0f);  // 检测点在身体中心下方0.6个方块是否有碰撞箱
        BlockHitResult HitResult = down1block.getCollisionShape(player.level(), down1pos).clip(player.position(), ClimbAnimTestPoint, down1pos);
        if (HitResult == null) { // 没有碰撞箱时
            return true;
        }
        else {
            return HitResult.getType() == BlockHitResult.Type.MISS;
        }
    }
}
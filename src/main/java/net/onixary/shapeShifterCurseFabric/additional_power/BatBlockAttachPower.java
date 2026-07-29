package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.networking.ModPacketsS2CServer;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimRegistries;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimUtils;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static net.onixary.shapeShifterCurseFabric.additional_power.BatAttachEventHandler.getBatAttachPower;

// XuHaoNan: 之后尝试重构这个逻辑 不过不太着急

public class BatBlockAttachPower extends Power {

    private final Predicate<Entity> entityAttachCondition;
    private final Predicate<BlockInWorld> blockCondition;
    private final Consumer<LivingEntity> sideAttachAction;
    private final Consumer<LivingEntity> bottomAttachAction;
    private final int bottomAttachInterval;

    // 吸附状态
    private boolean isAttached = false;
    private AttachType attachType = AttachType.NONE;
    private BlockPos attachedBlockPos = null;
    private Direction attachedSide = null;
    private int bottomAttachTimer = 0;

    public enum AttachType {
        NONE, SIDE, BOTTOM
    }

    public BatBlockAttachPower(PowerType<?> type, LivingEntity entity,
                               Predicate<Entity> entityAttachCondition,
                               Predicate<BlockInWorld> blockCondition,
                               Consumer<LivingEntity> sideAttachAction,
                               Consumer<LivingEntity> bottomAttachAction,
                               int bottomAttachInterval) {
        super(type, entity);
        this.entityAttachCondition = entityAttachCondition;
        this.blockCondition = blockCondition;
        this.sideAttachAction = sideAttachAction;
        this.bottomAttachAction = bottomAttachAction;
        this.bottomAttachInterval = bottomAttachInterval;
        this.setTicking(true);
        this.shouldTickWhenInactive();
    }

    public static void syncClientState(Player player, boolean isAttached, int attachTypeOrdinal,
                                       BlockPos attachedPos, Direction attachedSide) {
        // 获取玩家的BatBlockAttachPower实例
        BatBlockAttachPower power = getBatAttachPower(player);
        if (power != null) {
            power.isAttached = isAttached;
            power.attachType = AttachType.values()[attachTypeOrdinal];
            power.attachedBlockPos = attachedPos;
            power.attachedSide = attachedSide;
            power.bottomAttachTimer = 0;

            // 如果是吸附状态，立即设置客户端位置
            if (isAttached && attachedPos != null && attachedSide != null) {
                Vec3 targetPos;
                if (power.attachType == AttachType.SIDE) {
                    targetPos = Vec3.atCenterOf(attachedPos).add(Vec3.atLowerCornerOf(new net.minecraft.core.Vec3i(attachedSide.getStepX(), attachedSide.getStepY(), attachedSide.getStepZ())).scale(0.75d)).add(0, -0.5, 0);
                } else {
                    targetPos = Vec3.atCenterOf(attachedPos).add(0, -1.5, 0);
                }

                player.setPos(targetPos.x, targetPos.y, targetPos.z);
                player.setDeltaMovement(Vec3.ZERO);
                //System.out.println("Debug: CLIENT position synced to " + targetPos);
            }

            //System.out.println("Debug: Client state synced - isAttached=" + isAttached +
            //        ", attachType=" + power.attachType);
        }

    }

    @Override
    public void tick() {
        super.tick();

        if (!(entity instanceof Player player)) {
            return;
        }

        // 只在服务端处理方块检查和detach逻辑
        if (!player.level().isClientSide()) {
            if (isAttached && attachedBlockPos != null) {
                Level world = entity.level();
                BlockState blockState = world.getBlockState(attachedBlockPos);

                //System.out.println("Debug: SERVER Tick checking block at " + attachedBlockPos + ", isAir: " + blockState.isAir());

                if (blockState.isAir()) {
                    //System.out.println("Debug: SERVER Block is air, force detaching");
                    detach(player,false);
                    return;
                }
            }

            // 服务端处理定时动作
            if (isAttached && attachType == AttachType.BOTTOM && bottomAttachAction != null) {
                bottomAttachTimer++;
                if (bottomAttachTimer >= bottomAttachInterval) {
                    bottomAttachAction.accept(entity);
                    bottomAttachTimer = 0;
                }
            }
        }

        // 客户端和服务端都执行位置维持
        if (isAttached) {
            maintainAttachPosition(player);

            if (attachType == AttachType.SIDE && attachedSide != null) {
                maintainFacingDirection(player);
            }
        }
    }

    public boolean tryAttach(Player player, BlockHitResult hitResult) {
        if (isAttached || player.onGround()) {
            return false;
        }

        if (this.entityAttachCondition != null && !this.entityAttachCondition.test(player)) {
            return false;
        }

        BlockPos blockPos = hitResult.getBlockPos();

        if (blockCondition != null && !blockCondition.test(new BlockInWorld(entity.level(), blockPos, true))) {
            //System.out.println("Debug: Block condition failed");
            return false;
        }

        Direction hitSide = hitResult.getDirection();

        boolean attached = false;
        if (hitSide == Direction.DOWN) {
            //System.out.println("Debug: Attaching to bottom");
            attachToBottom(player, blockPos);
            attached = true;
        } else if (hitSide.getAxis().isHorizontal()) {
            //System.out.println("Debug: Attaching to side");
            attachToSide(player, blockPos, hitSide);
            attached = true;
        }

        // 在设置位置后发送同步包
        if (attached && !player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
	        MinecraftServer server = player.level().getServer();
	        if (server != null && attachedBlockPos != null && attachedSide != null) {
		        // 添加小延迟确保位置设置生效
            player.level().getServer().execute(() -> {
                ModPacketsS2CServer.sendBatAttachState(serverPlayer, isAttached, attachType.ordinal(),
                        attachedBlockPos, attachedSide);
                // 广播给附近的其他玩家
                ModPacketsS2CServer.broadcastBatAttachState(serverPlayer, isAttached, attachType.ordinal(),
                        attachedBlockPos, attachedSide);
		        });
	        }
        }

        return attached;
    }

    private void attachToSide(Player player, BlockPos blockPos, Direction side) {
        isAttached = true;
        attachType = AttachType.SIDE;
        attachedBlockPos = blockPos;
        attachedSide = side;

        // 立即在服务端设置位置
        Vec3 attachPos = Vec3.atCenterOf(blockPos).add(Vec3.atLowerCornerOf(new net.minecraft.core.Vec3i(side.getStepX(), side.getStepY(), side.getStepZ())).scale(0.75d)).add(0, -0.5, 0);
        player.setPos(attachPos.x, attachPos.y, attachPos.z);
        player.setDeltaMovement(Vec3.ZERO);
        player.setOnGround(true);
        // player.hasImpulse = true; // removed in 1.21.11
        player.hurtMarked = true;

        // 修改动作 最好在服务器端执行 虽然客户端也能执行 但是客户端还是会发送包到服务器进行处理
        AnimUtils.playPowerAnimLoop(player, AnimRegistries.POWER_ANIM_ATTACH_SIDE, AnimUtils.AnimationSendSideType.ONLY_SERVER);
    }

    private void attachToBottom(Player player, BlockPos blockPos) {
        isAttached = true;
        attachType = AttachType.BOTTOM;
        attachedBlockPos = blockPos;
        attachedSide = Direction.DOWN;
        bottomAttachTimer = 0;

        // 立即在服务端设置位置
        Vec3 attachPos = Vec3.atCenterOf(blockPos).add(0, -1.5f, 0);
        player.setPos(attachPos.x, attachPos.y, attachPos.z);
        player.setDeltaMovement(Vec3.ZERO);
        player.setOnGround(true);
        // player.hasImpulse = true; // removed in 1.21.11
        player.hurtMarked = true;

        // 修改动作 最好在服务器端执行 虽然客户端也能执行 但是客户端还是会发送包到服务器进行处理
        AnimUtils.playPowerAnimLoop(player, AnimRegistries.POWER_ANIM_ATTACH_BOTTOM, AnimUtils.AnimationSendSideType.ONLY_SERVER);
    }


    public void detach(Player player, boolean isByJump) {
        if (!isAttached) {
            return;
        }

        //System.out.println("Debug: detached on SERVER");

        // 如果是侧面吸附并且有跳跃动作，执行动作
        if (attachType == AttachType.SIDE && sideAttachAction != null) {
            sideAttachAction.accept(entity);
        }

        // 重置状态
        isAttached = false;
        AttachType oldAttachType = attachType;
        attachType = AttachType.NONE;
        attachedBlockPos = null;
        attachedSide = null;
        bottomAttachTimer = 0;

        // 重置物理状态
        player.setOnGround(false);
        player.setDeltaMovement(Vec3.ZERO);
        player.push(0, 0.4f, 0);
        if(isByJump){
            // 获取玩家面向方向的水平向量
            float yaw = player.getYRot();
            double yawRadians = Math.toRadians(yaw);

            // 计算水平方向向量（忽略Y轴）
            double dirX = -Math.sin(yawRadians);
            double dirZ = Math.cos(yawRadians);

            // 设置推进速度（可以调整0.5这个数值来改变推进力度）
            double jumpSpeed = 1.25f;
            // 同时添加向上的速度
            player.push(dirX * jumpSpeed, 0.4f, dirZ * jumpSpeed);
        }
        // player.hasImpulse = true; // removed in 1.21.11
        player.hurtMarked = true;

        // 同步到客户端
        if (player instanceof ServerPlayer serverPlayer) {
            ModPacketsS2CServer.sendBatAttachState(serverPlayer, false, AttachType.NONE.ordinal(), null, null);
            // 广播给附近的其他玩家
            ModPacketsS2CServer.broadcastBatAttachState(serverPlayer, false, AttachType.NONE.ordinal(), null, null);
        }

        // 修改动作 最好在服务器端执行 虽然客户端也能执行 但是客户端还是会发送包到服务器进行处理
        AnimUtils.stopPowerAnimWithIDs(player, AnimUtils.AnimationSendSideType.ONLY_SERVER, AnimRegistries.POWER_ANIM_ATTACH_SIDE, AnimRegistries.POWER_ANIM_ATTACH_BOTTOM);
    }


    private void maintainAttachPosition(Player player) {
        if (attachedBlockPos == null || attachedSide == null) {
            return;
        }

        Vec3 targetPos;
        if (attachType == AttachType.SIDE) {
            targetPos = Vec3.atCenterOf(attachedBlockPos).add(Vec3.atLowerCornerOf(new net.minecraft.core.Vec3i(attachedSide.getStepX(), attachedSide.getStepY(), attachedSide.getStepZ())));
        } else {
            targetPos = Vec3.atCenterOf(attachedBlockPos).add(0, -1.5f, 0);
        }

        // 类似蜂蜜块的完全停止效果
        Vec3 currentVelocity = player.getDeltaMovement();

        // 完全重置速度，类似蜘蛛网效果
        player.setDeltaMovement(0, 0, 0);

        // 强制设置位置
        player.setPos(targetPos.x, targetPos.y, targetPos.z);

        // 模拟蜂蜜块的粘性效果
        player.makeStuckInBlock(player.getInBlockState(), new Vec3(0.0, 0.0, 0.0));

        // 设置为在地面状态避免摔落伤害
        player.setOnGround(true);
        player.fallDistance = 0;

        // 直接操作移动相关字段，类似原版的处理方式
        // walkDist removed in 1.21.11
        player.moveDist = 0;
        player.flyDist = 0;

        // 重置移动输入相关字段
        player.xxa = 0;
        player.yya = 0;
        player.zza = 0;

        // 标记速度已变更
        // player.hasImpulse = true; // removed in 1.21.11
        player.hurtMarked = true;

    }

    private void maintainFacingDirection(Player player) {
        if (attachedSide == null) {
            return;
        }

        // 计算面向吸附表面的角度
        float targetYaw = getTargetYaw();

        player.setYBodyRot(targetYaw);
        player.yBodyRotO = targetYaw;
    }

    // Getter 方法
    public float getTargetYaw() {
        if (attachedSide == null) {
            return entity.getYRot();
        }
        return switch (attachedSide) {
            case NORTH -> 0.0f;
            case SOUTH -> 180.0f;
            case WEST -> -90.0f;
            case EAST -> 90.0f;
            default -> entity.getYRot();
        };
    }

    public boolean isAttached() {
        return isAttached;
    }

    public AttachType getAttachType() {
        return attachType;
    }

    public BlockPos getAttachedBlockPos() {
        return attachedBlockPos;
    }

    public Direction getAttachedSide() {
        return attachedSide;
    }

    // 用于处理跳跃取消吸附
    public void handleJump(Player player) {
        if (isAttached) {
            detach(player, true);
        }
    }

    // 用于处理右键取消吸附
    public void handleRightClick(Player player) {
        if (isAttached) {
            detach(player, false);
        }
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
                ShapeShifterCurseFabric.identifier("bat_block_attach"),
                new SerializableData()
                        .add("attach_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                        .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                        .add("side_attach_action", ApoliDataTypes.ENTITY_ACTION, null)
                        .add("bottom_attach_action", ApoliDataTypes.ENTITY_ACTION, null)
                        .add("bottom_attach_interval", SerializableDataTypes.INT, 20),
                data -> (type, entity) -> new BatBlockAttachPower(
                        type,
                        entity,
                        data.get("attach_condition"),
                        data.get("block_condition"),
                        data.get("side_attach_action"),
                        data.get("bottom_attach_action"),
                        data.getInt("bottom_attach_interval"))
        ).allowCondition();
    }
}
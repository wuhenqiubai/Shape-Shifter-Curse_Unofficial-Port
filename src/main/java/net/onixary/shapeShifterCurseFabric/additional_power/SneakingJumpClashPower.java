package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import java.util.function.Consumer;

public class SneakingJumpClashPower extends Power {

    private final Consumer<Tuple<Entity, Entity>> bientityAction;
    private final int checkDuration;
    private final double expansionDistance;
    private final float damage;
    
    private boolean isActive = false;
    private int activeTicks = 0;
    private boolean wasOnGround = true;

    public SneakingJumpClashPower(PowerType<?> type, LivingEntity entity,
                                  Consumer<Tuple<Entity, Entity>> bientityAction,
                                 int checkDuration, double expansionDistance, float damage) {
        super(type, entity);
        this.bientityAction = bientityAction;
        this.checkDuration = checkDuration;
        this.expansionDistance = expansionDistance;
        this.damage = damage;
        this.setTicking(true);
    }

    @Override
    public void tick() {
        if (!(entity instanceof Player player) || entity.level().isClientSide()) {
            return;
        }

        // 检查是否重新接触地面
        if (player.onGround()) {
            wasOnGround = true;
            // 如果之前处于激活状态，则重置状态
            if (isActive) {
                isActive = false;
                activeTicks = 0;
            }
        } else if (wasOnGround && player.isShiftKeyDown() && player.getDeltaMovement().y > 0) {
            // 从地面潜行跳跃时触发
            isActive = true;
            activeTicks = 0;
            wasOnGround = false;
        }

        // 如果power处于激活状态
        if (isActive) {
            activeTicks++;
            
            // 检查是否超过持续时间
            if (activeTicks > checkDuration) {
                isActive = false;
                activeTicks = 0;
                return;
            }
            
            // 检查碰撞
            if (checkForCollision(player)) {
                isActive = false;
                activeTicks = 0;
            }
        }
    }

    private boolean checkForCollision(Player player) {
        // 获取玩家面向方向
        Direction facing = player.getDirection();
        Vec3 facingVec = Vec3.atLowerCornerOf(facing.getNormal());
        
        // 扩展玩家碰撞箱向前方
        AABB expandedBox = player.getBoundingBox().expandTowards(facingVec.scale(expansionDistance)).inflate(0.5);
        
        // 查找碰撞的生物实体
        for (LivingEntity target : player.level().getEntitiesOfClass(
                LivingEntity.class, expandedBox, 
                e -> e != player && e.isAlive() && !e.isRemoved())) {

            if (player.level().clip(new ClipContext(new Vec3(player.getX(), player.getY(0.5f), player.getZ()), new Vec3(target.getX(), target.getY(0.5f), target.getZ()), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getType() == HitResult.Type.BLOCK) {
                continue;
            }

            // 触发碰撞action
            if (bientityAction != null) {
                this.bientityAction.accept(new Tuple<>(player, target));
            }

            // 触发伤害
            target.hurt(player.damageSources().playerAttack(player), damage);
            return true; // 发现碰撞，返回true
        }
        
        return false; // 未发现碰撞
    }

    public static PowerFactory<?> createFactory() {
        return new PowerFactory<>(
                ShapeShifterCurseFabric.identifier("sneaking_jump_clash"),
                new SerializableData()
                        .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null)
                        .add("check_duration", SerializableDataTypes.INT, 20)
                        .add("expansion_distance", SerializableDataTypes.DOUBLE, 1.0)
                        .add("damage", SerializableDataTypes.FLOAT, 1.0f),
                data -> (type, entity) -> new SneakingJumpClashPower(
                        type,
                        entity,
                        data.get("bientity_action"),
                        data.getInt("check_duration"),
                        data.getDouble("expansion_distance"),
                        data.getFloat("damage")
                )
        ).allowCondition();
    }
}

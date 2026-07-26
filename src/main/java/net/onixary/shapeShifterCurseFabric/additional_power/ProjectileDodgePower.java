package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import java.util.List;
import java.util.function.Predicate;

public class ProjectileDodgePower extends Power {
    private final ActionFactory<Entity>.Instance action;
    private final Predicate<Entity> entityCondition;
    private final double range;
    private final double dodgeSpeed;
    private final double triggerDistance;
    private final int cooldown;
    private int timer;
    private boolean dodgeRight;

    public ProjectileDodgePower(PowerType<?> type, LivingEntity entity,
                                ActionFactory<Entity>.Instance action,
                                Predicate<Entity> entityCondition,
                                double range, double dodgeSpeed,
                                double triggerDistance, int cooldown) {
        super(type, entity);
        this.action = action;
        this.entityCondition = entityCondition;
        this.range = range;
        this.dodgeSpeed = dodgeSpeed;
        this.triggerDistance = triggerDistance;
        this.cooldown = cooldown;
        this.timer = 0;
        this.dodgeRight = true;
        this.setTicking(true);
    }

    @Override
    public void tick() {
        if (!(this.entity instanceof Player player) || player.isSpectator()) return;
        if (player.level().isClientSide()) return;

        if(!entityCondition.test(player)) {return;}

        // 获取玩家周围的投射物
        List<Projectile> projectiles = player.level()
                .getEntitiesOfClass(Projectile.class,
                        player.getBoundingBox().inflate(range),
                        projectile -> {
                            // 排除玩家自己的投射物
                            if (projectile.getOwner() == player) {
                                return false;
                            }

                            // 使用多重条件检查投射物是否在飞行中
                            if (projectile.onGround()) {
                                return false;
                            }

                            // 检查inGround字段(如果可能通过Mixin访问)
                            // 如果无法通过Mixin，则跳过此检查

                            // 检查投射物的移动是否实际发生
                            Vec3 prevPos = projectile.xo != 0 ? new Vec3(projectile.xo, projectile.yo, projectile.zo) : null;
                            if (prevPos != null) {
                                double movement = prevPos.distanceTo(projectile.position());
	                            return !(movement < 0.1); // 投射物没有移动，可能已经停止
                            }

                            //ShapeShifterCurseFabric.LOGGER.info("飞行中投射物: " + projectile.getUuid());
                            return true;
                        });

        if (timer > 0) {
            timer--;
        }

        for (Projectile projectile : projectiles) {
            // 检查投射物是否朝玩家飞来
            if (isProjectileApproaching(projectile, player)) {
                // 执行躲避
                if (timer <= 0) {
                    performDodge(player, projectile);
                    timer = cooldown; // 设置冷却
                }
                break; // 一次只躲避一个投射物
            }
        }
    }

    private boolean isProjectileApproaching(Projectile projectile, Player player) {
        // 检查距离是否在触发范围内
        double distance = projectile.position().distanceTo(player.position());
        if (distance > triggerDistance) return false;

        // 计算投射物到玩家的方向向量
        Vec3 toPlayer = player.position().subtract(projectile.position()).normalize();

        // 获取投射物的速度向量并归一化
        Vec3 projectileVelocity = projectile.getDeltaMovement().normalize();

        // 使用点积判断投射物是否朝向玩家（值越大，方向越接近）
        double dot = toPlayer.dot(projectileVelocity);

        // 如果点积大于0.7，表示投射物大致朝向玩家（夹角小于约45度）
        return dot > 0.7;
    }

    private void performDodge(Player player, Projectile projectile) {
        // 获取投射物速度的水平分量
        Vec3 projectileVel = projectile.getDeltaMovement();
        Vec3 horizontalVel = new Vec3(projectileVel.x, 0, projectileVel.z).normalize();

        // 随机选择躲避方向
        dodgeRight = !dodgeRight; // 交替方向，避免单一方向

        // 计算垂直方向向量（左右）
        Vec3 dodgeDirection;
        if (dodgeRight) {
            dodgeDirection = new Vec3(-horizontalVel.z, 0, horizontalVel.x).normalize();
        } else {
            dodgeDirection = new Vec3(horizontalVel.z, 0, -horizontalVel.x).normalize();
        }
	    ShapeShifterCurseFabric.LOGGER.info("正在躲避投射物: {}", projectile.getUUID());
        // 应用躲避速度
        player.push(dodgeDirection.scale(dodgeSpeed));
        player.hurtMarked = true;
        if (action != null) {
            action.accept(player);
        }
    }

    public static PowerFactory<?> createFactory() {
        return new PowerFactory<>(
                ShapeShifterCurseFabric.identifier("projectile_dodge"),
                new SerializableData()
                        .add("action", ApoliDataTypes.ENTITY_ACTION, null)
                        .add("entity_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                        .add("range", SerializableDataTypes.DOUBLE, 5.0)
                        .add("dodge_speed", SerializableDataTypes.DOUBLE, 1.0)
                        .add("trigger_distance", SerializableDataTypes.DOUBLE, 4.0)
                        .add("cooldown", SerializableDataTypes.INT, 20),
                data -> (powerType, entity) -> new ProjectileDodgePower(
                        powerType,
                        entity,
                        data.get("action"),
                        data.get("entity_condition"),
                        data.getDouble("range"),
                        data.getDouble("dodge_speed"),
                        data.getDouble("trigger_distance"),
                        data.getInt("cooldown")
                )
        ).allowCondition();
    }
}

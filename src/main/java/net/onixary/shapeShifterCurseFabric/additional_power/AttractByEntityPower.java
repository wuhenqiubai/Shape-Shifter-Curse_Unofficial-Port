package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class AttractByEntityPower extends Power{

    private final Predicate<Entity> entityCondition;
    private final ActionFactory<Entity>.Instance entityAction;
    private final ActionFactory<Entity>.Instance selfAction;
    private final float attractionSpeed;
    private final float attractionRadius;
    private final float stopRadius;
    private final float escapeAttractionSpeed;
    private final float escapeAngleThreshold;

    private int tickCounter = 0;

    private Entity targetEntity;

    public AttractByEntityPower(PowerType<?> type, LivingEntity entity,
                                Predicate<Entity> entityCondition,
                                ActionFactory<Entity>.Instance entityAction,
                                ActionFactory<Entity>.Instance selfAction,
                                float attractionSpeed,
                                float attractionRadius,
                                float stopRadius,
                                float escapeAttractionSpeed,
                                float escapeAngleThreshold) {
        super(type, entity);
        this.entityCondition = entityCondition;
        this.entityAction = entityAction;
        this.selfAction = selfAction;
        this.attractionSpeed = attractionSpeed;
        this.attractionRadius = attractionRadius;
        this.stopRadius = stopRadius;
        this.escapeAttractionSpeed = escapeAttractionSpeed;
        this.escapeAngleThreshold = escapeAngleThreshold;
        this.setTicking(true);
    }

    @Override
    public void tick() {
        if (!(entity instanceof Player player) || player.isSpectator()) {
            return;
        }

        if (tickCounter++ % 5 == 0) {
            // 1. 检测范围内的所有实体
            AABB searchBox = AABB.unitCubeFromLowerCorner(player.position()).inflate(attractionRadius);
            List<Entity> entities = player.level().getEntities(
                    player,
                    searchBox,
                    e -> entityCondition.test(e)
            );

            // 2. 找到最近的符合条件的实体
            targetEntity = entities.stream()
                    .filter(entity -> entity.isAlive() && !entity.isSpectator())
                    .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
                    .orElse(null);

            // 如果目标实体存在且距离小于停止半径，则不进行吸引
            if (targetEntity != null && player.distanceToSqr(targetEntity) < stopRadius * stopRadius) {
                targetEntity = null; // 重置目标实体
            }

            if (isPlayerInVehicle(player) || !player.onGround()) {
                targetEntity = null; // 清除目标
                return;
            }

            // 射线检测是否可以看到
            if (targetEntity != null) {
                Vec3 actorEyePos = entity.getEyePosition();
                Vec3 targetEyePos = targetEntity.getEyePosition();
                ClipContext context = new ClipContext(actorEyePos, targetEyePos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity);
                if(entity.level().clip(context).getType() == HitResult.Type.BLOCK){
                    targetEntity = null;
                }
            }
        }


        // 3. 应用吸引效果
        if (targetEntity != null) {
            Vec3 attractDirection = targetEntity.position()
                    .subtract(player.position())
                    .multiply(1, 0, 1) // 忽略Y轴
                    .normalize();

            // 计算当前速度方向
            Vec3 currentVelocity = player.getDeltaMovement();
            Vec3 horizontalVelocity = new Vec3(currentVelocity.x, 0, currentVelocity.z);
            // 计算角色面朝向
            Vec3 playerFacing = player.getLookAngle().multiply(1, 0, 1).normalize();

            // 计算实际应用的吸引力
            float effectiveSpeed = calculateEffectiveSpeed(attractDirection, horizontalVelocity, playerFacing);

            // 应用速度
            Vec3 finalVelocity = attractDirection.scale(effectiveSpeed).add(0, currentVelocity.y, 0);
            player.setDeltaMovement(finalVelocity);
            player.hurtMarked = true;

            // 4. 执行实体动作（如果存在）
            if (entityAction != null) {
                entityAction.accept(targetEntity);
            }

            // 5. 执行自身动作（如果存在）
            if (selfAction != null) {
                selfAction.accept(player);
            }

            // 6. 同步状态
            PowerHolderComponent.syncPower(entity, this.type);
        }
    }

    // 检查玩家是否在载具上
    private boolean isPlayerInVehicle(Player player) {
        Entity vehicle = player.getVehicle();

        // 检查所有已知载具类型
        return vehicle != null && (
                vehicle instanceof Boat ||
                        vehicle instanceof Minecart ||
                        // 支持其他模组载具
                        vehicle.getType().toShortString().contains("vehicle") ||
                        vehicle.getType().toShortString().contains("mount")
        );
    }

    // 计算实际应用的吸引力速度（考虑逃脱机制）
    private float calculateEffectiveSpeed(Vec3 attractDirection, Vec3 playerVelocity, Vec3 playerFacing) {


        Vec3 faceDirection = playerFacing.normalize();

        double dotProduct = faceDirection.dot(attractDirection);

        double angle = Math.acos(Mth.clamp(dotProduct, -1.0, 1.0));

        // 5. 判断是否在逃脱状态（夹角大于阈值）
        if (dotProduct < 0) {
            // 应用逃脱衰减
            return escapeAttractionSpeed;
        }

        return attractionSpeed;
    }


    @Override
    public void onAdded() {
        super.onAdded();
        // 初始化时重置目标
        targetEntity = null;
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        // 移除时重置目标
        targetEntity = null;
    }

    // 获取当前目标实体（可用于其他逻辑）
    public Entity getTargetEntity() {
        return targetEntity;
    }

    // 工厂方法
    public static PowerFactory<?> getFactory() {
        return new PowerFactory<>(
                ShapeShifterCurseFabric.identifier("attract_by_entity"),
                new SerializableData()
                        .add("entity_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                        .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                        .add("self_action", ApoliDataTypes.ENTITY_ACTION, null)
                        .add("attraction_speed", SerializableDataTypes.FLOAT, 0.1f)
                        .add("attraction_radius", SerializableDataTypes.FLOAT, 8.0f)
                        .add("stop_radius", SerializableDataTypes.FLOAT, 1.0f)
                        .add("escape_attraction_speed", SerializableDataTypes.FLOAT, 0.025f)
                        .add("escape_angle", SerializableDataTypes.FLOAT, (float) Math.toRadians(80)),
                data -> (powerType, entity) -> new AttractByEntityPower(
                        powerType,
                        entity,
                        data.get("entity_condition"),
                        data.get("entity_action"),
                        data.get("self_action"),
                        data.getFloat("attraction_speed"),
                        data.getFloat("attraction_radius"),
                        data.getFloat("stop_radius"),
                        data.getFloat("escape_attraction_speed"),
                        data.getFloat("escape_angle")
                )
        ).allowCondition();
    }
}

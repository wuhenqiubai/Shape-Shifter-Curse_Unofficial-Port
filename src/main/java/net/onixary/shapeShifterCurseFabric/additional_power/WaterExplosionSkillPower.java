package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.ActiveCooldownPower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import java.util.List;

// 特效由AI整的 我是真的不太会扣这种效果类的值 如果需要修改特效 可以让AI重写部分函数(spawnEffects applyPull)
// 目前的有点太强了 得砍一下参数 或增加部分风险
// 目前加了吸到的生物无法移动 无法近战攻击(要是能攻击那这技能就废了) 吸10tick后再炸 基本能范围清怪 不过我感觉有点太强了 得砍或提升其他技能的强度
public class WaterExplosionSkillPower extends ActiveCooldownPower {
    private static final ParticleOptions PULL_PARTICLE = ParticleTypes.RAIN;
    private static final double PULL_PARTICLE_SPEED = 0.35;  // 坏消息 大部分粒子不吃动量
    private static final SoundEvent PULL_SOUND = SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_INSIDE;
    private static final int PARTICLE_COUNT = 25;
    private static final int SOUND_INTERVAL = 5;
    private static final float SOUND_VOLUME = 0.6f;
    private static final float SOUND_PITCH = 1.0f;

    private final double pullRadius;
    private final double pullStrength;
    private final int pullDuration;
    private final boolean verticalPull;
    private final double maxSpeed;
    private final ConditionFactory<Tuple<Entity, Entity>>.Instance entityCondition;
    private final ConditionFactory<Entity>.Instance condition;
    private final ActionFactory<Entity>.Instance onStartAction;
    private final ActionFactory<Entity>.Instance onDoneAction;
    private final ActionFactory<Tuple<Entity, Entity>>.Instance onPullAction;

    private int currentTick;

    public WaterExplosionSkillPower(PowerType<?> type, LivingEntity entity, int cooldownDuration, HudRender hudRender, SerializableData.Instance data) {
        super(type, entity, cooldownDuration, hudRender, (e) -> {});
        this.pullRadius = data.getDouble("pull_radius");
        this.pullStrength = data.getDouble("pull_strength");
        this.pullDuration = data.getInt("pull_duration");
        this.verticalPull = data.getBoolean("vertical_pull");
        this.maxSpeed = data.getDouble("max_speed");
        this.entityCondition = data.get("entity_condition");
        this.condition = data.get("can_start_condition");
        this.onStartAction = data.get("on_start_action");
        this.onDoneAction = data.get("on_done_action");
        this.onPullAction = data.get("on_pull_action");
        this.setKey(data.get("key"));
        setTicking(false);
    }

    @Override
    public void tick() {
        if (currentTick > 0) {
            tickPull();
        }
    }

    private void tickPull() {
        applyPull();
        spawnEffects();
        currentTick--;
        if (currentTick == 0) {
            if (onDoneAction != null) {
                onDoneAction.accept(entity);
            }
            currentTick = -1;
        }
    }

    private void applyPull() {
        Vec3 center = entity.position();
        AABB box = new AABB(
                center.x - pullRadius, center.y - pullRadius, center.z - pullRadius,
                center.x + pullRadius, center.y + pullRadius, center.z + pullRadius
        );
        List<LivingEntity> targets = entity.level().getEntitiesOfClass(LivingEntity.class, box, e -> e != entity);
        double radiusSq = pullRadius * pullRadius;
        for (LivingEntity target : targets) {
            if (target.distanceToSqr(center) > radiusSq) continue;
            if (entityCondition != null && !entityCondition.test(new Tuple<>(entity, target))) continue;
            Vec3 direction = center.subtract(target.position());
            if (!verticalPull) {
                direction = new Vec3(direction.x, 0.0, direction.z);
            }
            if (direction.lengthSqr() < 1.0E-6) continue;
            direction = direction.normalize().scale(pullStrength);
            Vec3 newVelocity = target.getDeltaMovement().add(direction);
            double speed = newVelocity.length();
            if (speed > maxSpeed) {
                newVelocity = newVelocity.scale(maxSpeed / speed);
            }
            if (onPullAction != null) {
                onPullAction.accept(new Tuple<>(entity, target));
            }
            target.setDeltaMovement(newVelocity);
            target.hurtMarked = true;
        }
    }

    private void spawnEffects() {
        if (!(entity.level() instanceof ServerLevel serverWorld)) return;
        Vec3 center = entity.position();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            double angle = serverWorld.random.nextDouble() * Math.PI * 2.0;
            double dist = pullRadius * (0.3 + serverWorld.random.nextDouble() * 0.7);
            double offsetX = Math.cos(angle) * dist;
            double offsetZ = Math.sin(angle) * dist;
            double px = center.x + offsetX;
            double pz = center.z + offsetZ;
            double py = center.y + 0.5 + serverWorld.random.nextDouble() * 1.5;
            double len = Math.sqrt(offsetX * offsetX + offsetZ * offsetZ);
            double vx = 0.0;
            double vz = 0.0;
            if (len > 1.0E-4) {
                vx = -offsetX / len * PULL_PARTICLE_SPEED;
                vz = -offsetZ / len * PULL_PARTICLE_SPEED;
            }
            serverWorld.sendParticles(PULL_PARTICLE, px, py, pz, 1, vx, 0.0, vz, 1.0);
        }
        if (SOUND_INTERVAL > 0 && (pullDuration - currentTick) % SOUND_INTERVAL == 0) {
            serverWorld.playSound(
                    null,
                    center.x, center.y, center.z,
                    PULL_SOUND,
                    SoundSource.PLAYERS,
                    SOUND_VOLUME, SOUND_PITCH
            );
        }
    }

    @Override
    public void onUse() {
        if(canUse()) {
            if (condition != null && !condition.test(entity)) return;
            super.onUse();
            if (onStartAction != null) {
                onStartAction.accept(entity);
            }
            currentTick = pullDuration;
        }
    }

    public static PowerFactory<?> createFactory() {
        return new PowerFactory<>(
                ShapeShifterCurseFabric.identifier("water_explosion_skill"),
                new SerializableData()
                        .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Active.Key())
                        .add("cooldown", SerializableDataTypes.INT, 0)
                        .add("hud_render", ApoliDataTypes.HUD_RENDER, HudRender.DONT_RENDER)
                        .add("pull_radius", SerializableDataTypes.DOUBLE, 6.0)
                        .add("pull_strength", SerializableDataTypes.DOUBLE, 0.3)
                        .add("pull_duration", SerializableDataTypes.INT, 5)
                        .add("vertical_pull", SerializableDataTypes.BOOLEAN, true)
                        .add("max_speed", SerializableDataTypes.DOUBLE, 3.0)
                        .add("entity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                        .add("can_start_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                        .add("on_start_action", ApoliDataTypes.ENTITY_ACTION, null)
                        .add("on_done_action", ApoliDataTypes.ENTITY_ACTION, null)
                        .add("on_pull_action", ApoliDataTypes.BIENTITY_ACTION, null),
                data -> (type, player) -> new WaterExplosionSkillPower(
                        type, player,
                        data.getInt("cooldown"),
                        data.get("hud_render"),
                        data
                )
        ).allowCondition();
    }
}
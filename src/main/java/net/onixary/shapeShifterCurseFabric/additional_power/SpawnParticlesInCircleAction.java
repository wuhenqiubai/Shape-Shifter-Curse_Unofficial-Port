package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import java.util.function.Predicate;

public class SpawnParticlesInCircleAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        // 检查世界是否为服务器世界
        if (!(entity.level() instanceof ServerLevel serverWorld)) {
            return;
        }

        // 获取配置参数
        ParticleOptions particleEffect = data.get("particle");
        Predicate<Tuple<Entity, Entity>> biEntityCondition = data.get("bientity_condition");

        boolean force = data.getBoolean("force");
        float speed = data.getFloat("speed");
        int count = Math.max(0, data.getInt("count"));

        // 获取扩散/偏移参数
        Vec3 spread = data.get("spread");
        double offsetX = data.getDouble("offset_x");
        double offsetY = data.getDouble("offset_y");
        double offsetZ = data.getDouble("offset_z");

        // 获取环状参数
        double radius = data.getDouble("radius");
        int sampleCount = Math.max(1, data.getInt("sample_count"));

        // 计算基础位置（环的中心）
        Vec3 entityPos = entity.position();
        Vec3 basePos = entityPos.add(offsetX, offsetY, offsetZ);

        // 计算扩散向量（相对于实体尺寸）
        Vec3 delta = spread.multiply(entity.getBbWidth(), entity.getEyeHeight(entity.getPose()), entity.getBbWidth());

        // 遍历环上的每个采样点
        for (int i = 0; i < sampleCount; i++) {
            // 计算当前点在圆环上的角度（弧度）
            double angle = 2 * Math.PI * i / sampleCount;

            // 计算该点相对于环中心的XZ偏移
            double xOffset = radius * Math.cos(angle);
            double zOffset = radius * Math.sin(angle);

            // 计算最终粒子生成位置（Y轴保持不变，在XZ平面上形成环）
            Vec3 particlePos = basePos.add(xOffset, 0, zOffset);

            // Build the particle packet once
            var packet = new net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket(
                    particleEffect, force, false,
                    particlePos.x(), particlePos.y(), particlePos.z(),
                    (float) delta.x(), (float) delta.y(), (float) delta.z(),
                    speed, count
            );
            // Send to each matching player individually
            for (ServerPlayer player : serverWorld.players()) {
                if (biEntityCondition == null || biEntityCondition.test(new Tuple<>(entity, player))) {
                    serverWorld.sendParticles(player, force, particlePos.x(), particlePos.y(), particlePos.z(), packet);
                }
            }
        }
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
                ShapeShifterCurseFabric.identifier("spawn_particles_in_circle"),
                new SerializableData()
                        .add("particle", SerializableDataTypes.PARTICLE_EFFECT_OR_TYPE)
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                        .add("count", SerializableDataTypes.INT, 1)
                        .add("speed", SerializableDataTypes.FLOAT, 0.0F)
                        .add("force", SerializableDataTypes.BOOLEAN, false)
                        .add("spread", SerializableDataTypes.VECTOR, new Vec3(0.5, 0.5, 0.5))
                        .add("offset_x", SerializableDataTypes.DOUBLE, 0.0D)
                        .add("offset_y", SerializableDataTypes.DOUBLE, 0.5D)
                        .add("offset_z", SerializableDataTypes.DOUBLE, 0.0D)
                        .add("radius", SerializableDataTypes.DOUBLE, 1.0D)      // 环的半径
                        .add("sample_count", SerializableDataTypes.INT, 8),    // 采样点数量（默认8个点形成圆环）
                SpawnParticlesInCircleAction::action
        );
    }
}

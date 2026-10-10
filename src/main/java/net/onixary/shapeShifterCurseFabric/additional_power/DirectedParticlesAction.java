package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

/** Initial velocity only: the selected particle retains its vanilla physics and lifetime. */
public final class DirectedParticlesAction {
    public enum Direction { ACTOR_TO_TARGET, TARGET_TO_ACTOR }

    static ClientboundLevelParticlesPacket packet(SerializableData.Instance data, Entity actor, Entity target) {
        Vec3 a = actor.getBoundingBox().getCenter().add(data.<Vec3>get("actor_offset"));
        Vec3 b = target.getBoundingBox().getCenter().add(data.<Vec3>get("target_offset"));
        boolean reverse = data.<Direction>get("direction") == Direction.TARGET_TO_ACTOR;
        Vec3 start = reverse ? b : a;
        Vec3 direction = (reverse ? a.subtract(b) : b.subtract(a)).normalize();
        // Count zero means ONE directed particle in the vanilla packet protocol.
        return new ClientboundLevelParticlesPacket(data.get("particle"), data.getBoolean("force"), start.x, start.y, start.z,
                (float) direction.x, (float) direction.y, (float) direction.z,
                Math.max(0, data.getFloat("speed")), 0);
    }

    public static ActionFactory<Tuple<Entity, Entity>> getFactory() {
        return new ActionFactory<>(ShapeShifterCurseFabric.identifier("directed_particles"), new SerializableData()
                .add("particle", SerializableDataTypes.PARTICLE_EFFECT_OR_TYPE)
                .add("direction", SerializableDataType.enumValue(Direction.class), Direction.ACTOR_TO_TARGET)
                .add("actor_offset", SerializableDataTypes.VECTOR, Vec3.ZERO)
                .add("target_offset", SerializableDataTypes.VECTOR, Vec3.ZERO)
                .add("speed", SerializableDataTypes.FLOAT, 0.3f)
                .add("count", SerializableDataTypes.INT, 1)
                .add("force", SerializableDataTypes.BOOLEAN, false), (data, pair) -> {
            Entity actor = pair.getA(), target = pair.getB();
            if (actor == null || target == null || !(actor.level() instanceof ServerLevel world)
                    || target.level() != world || actor.isRemoved() || target.isRemoved()) return;
            int count = Math.max(0, data.getInt("count"));
            if (count == 0) return;
            var packet = packet(data, actor, target);
            // Yarn ServerWorld.getPlayers() → Mojmap ServerLevel.players()（带 Predicate 的重载才是 getPlayers）
            for (var viewer : world.players()) {
                for (int i = 0; i < count; i++) {
                    if (!world.sendParticles(viewer, data.getBoolean("force"), packet.getX(), packet.getY(), packet.getZ(), packet)) break;
                }
            }
        });
    }
}

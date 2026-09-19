package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class SpawnParticlesAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if(entity.level().isClientSide) {
            return;
        }
        ServerLevel serverWorld = (ServerLevel) entity.level();
        int count = data.get("count");
        if(count <= 0)
            return;
        float speed = data.get("speed");
        Vec3 spread = data.get("spread");
        float deltaX = (float) (entity.getBbWidth() * spread.x);
        float deltaY = (float) (entity.getBbHeight() * spread.y);
        float deltaZ = (float) (entity.getBbWidth() * spread.z);
        float offsetY = entity.getBbHeight() * data.getFloat("offset_y");
        serverWorld.sendParticles(data.get("particle"), entity.getX(), entity.getY() + offsetY, entity.getZ(), count, deltaX, deltaY, deltaZ, speed);
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apoli.identifier("spawn_particles"),
            new SerializableData()
                .add("particle", SerializableDataTypes.PARTICLE_EFFECT_OR_TYPE)
                .add("count", SerializableDataTypes.INT)
                .add("speed", SerializableDataTypes.FLOAT, 0.0F)
                .add("force", SerializableDataTypes.BOOLEAN, false)
                .add("spread", SerializableDataTypes.VECTOR, new Vec3(0.5, 0.25, 0.5))
                .add("offset_y", SerializableDataTypes.FLOAT, 0.5F),
            SpawnParticlesAction::action
        );
    }
}

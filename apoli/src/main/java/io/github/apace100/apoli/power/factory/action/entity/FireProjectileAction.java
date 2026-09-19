package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.Consumer;

public class FireProjectileAction {

    public static void action(SerializableData.Instance data, Entity entity) {

        if (entity.level().isClientSide) return;

        ServerLevel serverWorld = (ServerLevel) entity.level();
        int count = data.get("count");

        for (int i = 0; i < count; i++) {

            EntityType<?> entityType = data.get("entity_type");
            CompoundTag entityNbt = data.get("tag");
            float yaw = entity.getYRot();
            float pitch = entity.getXRot();

            Optional<Entity> opt$entityToSpawn = MiscUtil.getEntityWithPassengers(
                serverWorld,
                entityType,
                entityNbt,
                entity.position().add(0, entity.getEyeHeight(entity.getPose()), 0),
                yaw,
                pitch
            );
            if (opt$entityToSpawn.isEmpty()) return;

            Vec3 velocity = entity.getDeltaMovement();
            Entity entityToSpawn = opt$entityToSpawn.get();
            RandomSource random = serverWorld.getRandom();

            float divergence = data.get("divergence");
            float speed = data.get("speed");

            if (entityToSpawn instanceof Projectile projectileToSpawn) {

                if (projectileToSpawn instanceof AbstractHurtingProjectile explosiveProjectileToSpawn) {
                    explosiveProjectileToSpawn.accelerationPower = speed;
                }

                projectileToSpawn.setOwner(entity);
                projectileToSpawn.shootFromRotation(entity, pitch, yaw, 0F, speed, divergence);

            }

            else {

                float  j = 0.017453292F;
                double k = 0.007499999832361937D;

                float l = -Mth.sin(yaw * j) * Mth.cos(pitch * j);
                float m = -Mth.sin(pitch * j);
                float n =  Mth.cos(yaw * j) * Mth.cos(pitch * j);

                Vec3 vec3d = new Vec3(l, m, n)
                    .normalize()
                    .add(random.nextGaussian() * k * divergence, random.nextGaussian() * k * divergence, random.nextGaussian() * k * divergence)
                    .scale(speed);

                entityToSpawn.setDeltaMovement(vec3d);
                entityToSpawn.push(velocity.x, entity.onGround() ? 0.0D : velocity.y, velocity.z);

            }

            serverWorld.tryAddFreshEntityWithPassengers(entityToSpawn);
            data.<Consumer<Entity>>ifPresent("projectile_action", projectileAction -> projectileAction.accept(entityToSpawn));

        }

    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("fire_projectile"),
            new SerializableData()
                .add("entity_type", SerializableDataTypes.ENTITY_TYPE)
                .add("divergence", SerializableDataTypes.FLOAT, 1F)
                .add("speed", SerializableDataTypes.FLOAT, 1.5F)
                .add("count", SerializableDataTypes.INT, 1)
                .add("tag", SerializableDataTypes.NBT, null)
                .add("projectile_action", ApoliDataTypes.ENTITY_ACTION, null),
            FireProjectileAction::action
        );
    }

}

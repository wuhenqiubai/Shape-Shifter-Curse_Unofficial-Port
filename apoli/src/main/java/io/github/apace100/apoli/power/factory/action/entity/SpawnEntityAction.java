package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class SpawnEntityAction {

    public static void action(SerializableData.Instance data, Entity entity) {

        if (entity.level().isClientSide()) return;

        ServerLevel serverWorld = (ServerLevel) entity.level();
        EntityType<?> entityType = data.get("entity_type");
        CompoundTag entityNbt = data.get("tag");

        Optional<Entity> opt$entityToSpawn = MiscUtil.getEntityWithPassengers(
            serverWorld,
            entityType,
            entityNbt,
            entity.position(),
            entity.getYRot(),
            entity.getXRot()
        );

        if (opt$entityToSpawn.isEmpty()) return;
        Entity entityToSpawn = opt$entityToSpawn.get();

        serverWorld.tryAddFreshEntityWithPassengers(entityToSpawn);
        data.<Consumer<Entity>>ifPresent("entity_action", entityAction -> entityAction.accept(entityToSpawn));

    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("spawn_entity"),
            new SerializableData()
                .add("entity_type", SerializableDataTypes.ENTITY_TYPE)
                .add("tag", SerializableDataTypes.NBT, null)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null),
            SpawnEntityAction::action
        );
    }

}

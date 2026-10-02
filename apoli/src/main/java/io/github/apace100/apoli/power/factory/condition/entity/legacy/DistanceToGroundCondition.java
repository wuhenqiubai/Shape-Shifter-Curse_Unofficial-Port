package io.github.apace100.apoli.power.factory.condition.entity.legacy;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

public class DistanceToGroundCondition {
    public static boolean condition(SerializableData.Instance data, Entity entity) {
        int distance = data.getInt("distance");

        if (distance <= 0)
            throw new IllegalArgumentException("Distance should not be <= 0!");

        BlockPos.MutableBlockPos pos = entity.blockPosition().mutable();
        for (int i = 0; i < distance; i++) {
            BlockState state = entity.level().getBlockState(pos.move(0, -1, 0));
            if (!state.isAir() && state.entityCanStandOnFace(entity.level(), pos, entity, Direction.UP)) {
                return true;
            }
        }

        return false;
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
            Apoli.legacy("distance_to_ground"),
            new SerializableData()
                .add("distance", SerializableDataTypes.INT),
            DistanceToGroundCondition::condition
        );
    }
}

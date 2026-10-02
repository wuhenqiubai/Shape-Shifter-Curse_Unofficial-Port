package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.AABB;

import java.util.function.Predicate;

public class BlockCollisionCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        AABB entityBoundingBox = entity.getBoundingBox();
        AABB offsetEntityBoundingBox = entityBoundingBox.move(
            data.getFloat("offset_x") * entityBoundingBox.getXsize(),
            data.getFloat("offset_y") * entityBoundingBox.getYsize(),
            data.getFloat("offset_z") * entityBoundingBox.getZsize()
        );

        if (data.isPresent("block_condition")) {

            Predicate<BlockInWorld> blockCondition = data.get("block_condition");
            BlockPos minBlockPos = BlockPos.containing(offsetEntityBoundingBox.minX + 0.001, offsetEntityBoundingBox.minY + 0.001, offsetEntityBoundingBox.minZ + 0.001);
            BlockPos maxBlockPos = BlockPos.containing(offsetEntityBoundingBox.maxX - 0.001, offsetEntityBoundingBox.maxY - 0.001, offsetEntityBoundingBox.maxZ - 0.001);
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            int matchingBlocks = 0;

            for (int x = minBlockPos.getX(); x <= maxBlockPos.getX(); x++) {
                for (int y = minBlockPos.getY(); y <= maxBlockPos.getY(); y++) {
                    for (int z = minBlockPos.getZ(); z <= maxBlockPos.getZ(); z++) {
                        mutableBlockPos.set(x, y, z);
                        if (blockCondition.test(new BlockInWorld(entity.level(), mutableBlockPos, true))) matchingBlocks++;
                    }
                }
            }

            return matchingBlocks > 0;

        }

        else return entity.level()
            .getBlockCollisions(entity, offsetEntityBoundingBox)
            .iterator()
            .hasNext();

    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("block_collision"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("offset_x", SerializableDataTypes.FLOAT, 0F)
                .add("offset_y", SerializableDataTypes.FLOAT, 0F)
                .add("offset_z", SerializableDataTypes.FLOAT, 0F),
            BlockCollisionCondition::condition
        );
    }

}

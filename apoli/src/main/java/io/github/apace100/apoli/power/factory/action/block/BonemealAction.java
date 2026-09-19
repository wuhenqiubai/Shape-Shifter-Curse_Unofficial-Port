package io.github.apace100.apoli.power.factory.action.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Triple;

public class BonemealAction {
    public static void action(SerializableData.Instance data, Triple<Level, BlockPos, Direction> block) {
        Level world = block.getLeft();
        BlockPos blockPos = block.getMiddle();
        Direction side = block.getRight();
        BlockPos blockPos2 = blockPos.relative(side);

        boolean spawnEffects = data.getBoolean("effects");

        if (BoneMealItem.growCrop(ItemStack.EMPTY, world, blockPos)) {
            if (spawnEffects && !world.isClientSide) {
                world.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, blockPos, 0);
            }
        } else {
            BlockState blockState = world.getBlockState(blockPos);
            boolean bl = blockState.isFaceSturdy(world, blockPos, side);
            if (bl && BoneMealItem.growWaterPlant(ItemStack.EMPTY, world, blockPos2, side)) {
                if (spawnEffects && !world.isClientSide) {
                    world.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, blockPos2, 0);
                }
            }
        }
    }

    public static ActionFactory<Triple<Level, BlockPos, Direction>> getFactory() {
        return new ActionFactory<>(Apoli.identifier("bonemeal"),
                new SerializableData()
                    .add("effects", SerializableDataTypes.BOOLEAN, true),
                BonemealAction::action
        );
    }
}

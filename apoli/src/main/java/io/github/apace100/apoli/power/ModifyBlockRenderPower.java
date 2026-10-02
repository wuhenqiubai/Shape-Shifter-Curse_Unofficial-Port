package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.ApoliClient;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.function.Predicate;

public class ModifyBlockRenderPower extends Power {

    private final Predicate<BlockInWorld> predicate;
    private final BlockState blockState;

    public ModifyBlockRenderPower(PowerType<?> type, LivingEntity entity, Predicate<BlockInWorld> predicate, BlockState state) {
        super(type, entity);
        this.predicate = predicate;
        this.blockState = state;
    }

    public boolean doesPrevent(LevelReader world, BlockPos pos) {
        BlockInWorld cbp = new BlockInWorld(world, pos, true);
        return predicate == null || predicate.test(cbp);
    }

    public BlockState getBlockState() {
        return blockState;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void onAdded() {
        super.onAdded();
        ApoliClient.shouldReloadWorldRenderer = true;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void onRemoved() {
        super.onRemoved();
        ApoliClient.shouldReloadWorldRenderer = true;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("modify_block_render"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("block", SerializableDataTypes.BLOCK),
            data ->
                (type, player) -> new ModifyBlockRenderPower(type, player,
                    (ConditionFactory<BlockInWorld>.Instance)data.get("block_condition"),
                    ((Block)data.get("block")).defaultBlockState()));
    }
}

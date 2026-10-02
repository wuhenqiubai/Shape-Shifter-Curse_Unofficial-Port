package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.function.Predicate;

public class PreventBlockUsePower extends Power {

    private final Predicate<BlockInWorld> predicate;

    public PreventBlockUsePower(PowerType<?> type, LivingEntity entity, Predicate<BlockInWorld> predicate) {
        super(type, entity);
        this.predicate = predicate;
    }

    public boolean doesPrevent(LevelReader world, BlockPos pos) {
        if(predicate == null) {
            return true;
        }
        BlockInWorld cbp = new BlockInWorld(world, pos, true);
        return predicate.test(cbp);
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("prevent_block_use"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null),
            data ->
                (type, player) -> new PreventBlockUsePower(type, player,
                    data.get("block_condition")))
            .allowCondition();
    }
}

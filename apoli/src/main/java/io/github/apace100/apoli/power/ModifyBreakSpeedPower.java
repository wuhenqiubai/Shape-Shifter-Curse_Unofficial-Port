package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.List;
import java.util.function.Predicate;

public class ModifyBreakSpeedPower extends ValueModifyingPower {

    private final Predicate<BlockInWorld> predicate;

    public ModifyBreakSpeedPower(PowerType<?> type, LivingEntity entity, Predicate<BlockInWorld> predicate) {
        super(type, entity);
        this.predicate = predicate;
    }

    public boolean doesApply(LevelReader world, BlockPos pos) {
        if(predicate == null) {
            return true;
        }
        BlockInWorld cbp = new BlockInWorld(world, pos, true);
        return predicate.test(cbp);
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("modify_break_speed"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null),
            data ->
                (type, player) -> {
                    ModifyBreakSpeedPower power = new ModifyBreakSpeedPower(type, player, data.get("block_condition"));
                    data.ifPresent("modifier", power::addModifier);
                    data.<List<Modifier>>ifPresent("modifiers",
                        mods -> mods.forEach(power::addModifier)
                    );
                    return power;
                })
            .allowCondition();
    }
}

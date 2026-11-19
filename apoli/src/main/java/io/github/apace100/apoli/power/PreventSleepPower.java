package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public class PreventSleepPower extends Power {

    private final Predicate<BlockInWorld> predicate;
    private final String message;
    private final boolean allowSpawnPoint;

    public PreventSleepPower(PowerType<?> type, LivingEntity entity, Predicate<BlockInWorld> predicate, String message, boolean allowSpawnPoint) {
        super(type, entity);
        this.predicate = predicate;
        this.message = message;
        this.allowSpawnPoint = allowSpawnPoint;
    }

    public boolean doesPrevent(LevelReader world, BlockPos pos) {
        BlockInWorld cbp = new BlockInWorld(world, pos, true);
        return predicate.test(cbp);
    }

    public String getMessage() {
        return message;
    }

    public boolean doesAllowSpawnPoint() {
        return allowSpawnPoint;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("prevent_sleep"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("message", SerializableDataTypes.STRING, "origins.cant_sleep")
                .add("set_spawn_point", SerializableDataTypes.BOOLEAN, false),
            data ->
                (type, player) ->
                    new PreventSleepPower(type, player,
                        data.isPresent("block_condition") ? (ConditionFactory<BlockInWorld>.Instance)data.get("block_condition") : cbp -> true,
                        data.getString("message"), data.getBoolean("set_spawn_point")))
            .allowCondition();
    }
}

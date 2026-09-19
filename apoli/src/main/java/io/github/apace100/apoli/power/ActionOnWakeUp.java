package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.apache.commons.lang3.tuple.Triple;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ActionOnWakeUp extends Power {

    private final Predicate<BlockInWorld> blockCondition;
    private final Consumer<Entity> entityAction;
    private final Consumer<Triple<Level, BlockPos, Direction>> blockAction;

    public ActionOnWakeUp(PowerType<?> type, LivingEntity entity, Predicate<BlockInWorld> blockCondition, Consumer<Entity> entityAction, Consumer<Triple<Level, BlockPos, Direction>> blockAction) {
        super(type, entity);
        this.blockCondition = blockCondition;
        this.entityAction = entityAction;
        this.blockAction = blockAction;
    }

    public boolean doesApply(BlockPos pos) {
        BlockInWorld cbp = new BlockInWorld(entity.level(), pos, true);
        return doesApply(cbp);
    }

    public boolean doesApply(BlockInWorld pos) {
        return blockCondition == null || blockCondition.test(pos);
    }

    public void executeActions(BlockPos pos, Direction dir) {
        if(blockAction != null) {
            blockAction.accept(Triple.of(entity.level(), pos, dir));
        }
        if(entityAction != null) {
            entityAction.accept(entity);
        }
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("action_on_wake_up"),
            new SerializableData()
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("block_action", ApoliDataTypes.BLOCK_ACTION, null)
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null),
            data ->
                (type, player) -> new ActionOnWakeUp(type, player,
                    (ConditionFactory<BlockInWorld>.Instance)data.get("block_condition"),
                    (ActionFactory<Entity>.Instance)data.get("entity_action"),
                    (ActionFactory<Triple<Level, BlockPos, Direction>>.Instance)data.get("block_action")))
            .allowCondition();
    }
}

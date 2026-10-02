package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.apache.commons.lang3.tuple.Triple;

import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ActionOnBlockUsePower extends ActiveInteractionPower {

    private final Consumer<Entity> entityAction;
    private final Predicate<BlockInWorld> blockCondition;
    private final EnumSet<Direction> directions;
    private final Consumer<Triple<Level, BlockPos, Direction>> blockAction;

    public ActionOnBlockUsePower(PowerType<?> type, LivingEntity entity, EnumSet<InteractionHand> hands, InteractionResult actionResult, Predicate<ItemStack> itemCondition, Consumer<Tuple<Level, ItemStack>> heldItemAction, ItemStack itemResult, Consumer<Tuple<Level, ItemStack>> resultItemAction, Consumer<Entity> entityAction, Predicate<BlockInWorld> blockCondition, EnumSet<Direction> directions, Consumer<Triple<Level, BlockPos, Direction>> blockAction, int priority) {
        super(type, entity, hands, actionResult, itemCondition, heldItemAction, itemResult, resultItemAction, priority);
        this.entityAction = entityAction;
        this.blockCondition = blockCondition;
        this.directions = directions;
        this.blockAction = blockAction;
    }


    public boolean shouldExecute(BlockPos blockPos, Direction direction, InteractionHand hand, ItemStack heldStack) {
        if(!super.shouldExecute(hand, heldStack)) {
            return false;
        }
        if(!directions.contains(direction)) {
            return false;
        }
        return blockCondition == null || blockCondition.test(new BlockInWorld(entity.level(), blockPos, true));
    }

    public InteractionResult executeAction(BlockPos blockPos, Direction direction, InteractionHand hand) {
        if(blockAction != null) {
            blockAction.accept(Triple.of(entity.level(), blockPos, direction));
        }
        if(entityAction != null) {
            entityAction.accept(entity);
        }
        performActorItemStuff(this, (Player) entity, hand);
        return getActionResult();
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("action_on_block_use"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("block_action", ApoliDataTypes.BLOCK_ACTION, null)
                .add("directions", SerializableDataTypes.DIRECTION_SET, EnumSet.allOf(Direction.class))
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("hands", SerializableDataTypes.HAND_SET, EnumSet.allOf(InteractionHand.class))
                .add("result_stack", SerializableDataTypes.ITEM_STACK, null)
                .add("held_item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("result_item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("action_result", SerializableDataTypes.ACTION_RESULT, InteractionResult.SUCCESS)
                .add("priority", SerializableDataTypes.INT, 0),
            data ->
                (type, player) -> new ActionOnBlockUsePower(type, player,
                    data.get("hands"),
                    data.get("action_result"),
                    data.get("item_condition"),
                    data.get("held_item_action"),
                    data.get("result_stack"),
                    data.get("result_item_action"),
                    data.get("entity_action"),
                    data.get("block_condition"),
                    data.get("directions"),
                    data.get("block_action"),
                    data.get("priority")))
            .allowCondition();
    }
}

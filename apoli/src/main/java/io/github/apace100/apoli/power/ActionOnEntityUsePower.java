package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ActionOnEntityUsePower extends ActiveInteractionPower {

    private final Consumer<Tuple<Entity, Entity>> biEntityAction;
    private final Predicate<Tuple<Entity, Entity>> bientityCondition;

    public ActionOnEntityUsePower(PowerType<?> type, LivingEntity entity, EnumSet<InteractionHand> hands, InteractionResult actionResult, Predicate<ItemStack> itemCondition, Consumer<Tuple<Level, ItemStack>> heldItemAction, ItemStack itemResult, Consumer<Tuple<Level, ItemStack>> itemAction, Consumer<Tuple<Entity, Entity>> biEntityAction, Predicate<Tuple<Entity, Entity>> bientityCondition, int priority) {
        super(type, entity, hands, actionResult, itemCondition, heldItemAction, itemResult, itemAction, priority);
        this.biEntityAction = biEntityAction;
        this.bientityCondition = bientityCondition;
    }

    public boolean shouldExecute(Entity other, InteractionHand hand, ItemStack heldStack) {
        if(!super.shouldExecute(hand, heldStack)) {
            return false;
        }
        return bientityCondition == null || bientityCondition.test(new Tuple<>(entity, other));
    }

    public InteractionResult executeAction(Entity other, InteractionHand hand) {
        if(biEntityAction != null) {
            biEntityAction.accept(new Tuple<>(entity, other));
        }
        performActorItemStuff(this, (Player) entity, hand);
        return getActionResult();
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("action_on_entity_use"),
            new SerializableData()
                .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("hands", SerializableDataTypes.HAND_SET, EnumSet.allOf(InteractionHand.class))
                .add("result_stack", SerializableDataTypes.ITEM_STACK, null)
                .add("held_item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("result_item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("action_result", SerializableDataTypes.ACTION_RESULT, InteractionResult.SUCCESS)
                .add("priority", SerializableDataTypes.INT, 0),
            data ->
                (type, player) -> new ActionOnEntityUsePower(type, player,
                    data.get("hands"),
                    data.get("action_result"),
                    data.get("item_condition"),
                    data.get("held_item_action"),
                    data.get("result_stack"),
                    data.get("result_item_action"),
                    data.get("bientity_action"),
                    data.get("bientity_condition"),
                    data.get("priority")))
            .allowCondition();
    }
}


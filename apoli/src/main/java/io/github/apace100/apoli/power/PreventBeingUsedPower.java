package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
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

public class PreventBeingUsedPower extends InteractionPower {

    private final Consumer<Tuple<Entity, Entity>> biEntityAction;
    private final Predicate<Tuple<Entity, Entity>> bientityCondition;

    public PreventBeingUsedPower(PowerType<?> type, LivingEntity entity, EnumSet<InteractionHand> hands, InteractionResult actionResult, Predicate<ItemStack> itemCondition, Consumer<Tuple<Level, ItemStack>> heldItemAction, ItemStack itemResult, Consumer<Tuple<Level, ItemStack>> itemAction, Consumer<Tuple<Entity, Entity>> biEntityAction, Predicate<Tuple<Entity, Entity>> bientityCondition) {
        super(type, entity, hands, actionResult, itemCondition, heldItemAction, itemResult, itemAction);
        this.biEntityAction = biEntityAction;
        this.bientityCondition = bientityCondition;
    }

    public boolean doesApply(Player other, InteractionHand hand, ItemStack heldStack) {
        if(!shouldExecute(hand, heldStack)) {
            return false;
        }
        return bientityCondition == null || bientityCondition.test(new Tuple<>(other, entity));
    }

    public InteractionResult executeAction(Player other, InteractionHand hand) {
        if(biEntityAction != null) {
            biEntityAction.accept(new Tuple<>(other, entity));
        }
        performActorItemStuff(this, other, hand);
        return getActionResult();
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("prevent_being_used"),
            new SerializableData()
                .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("hands", SerializableDataTypes.HAND_SET, EnumSet.allOf(InteractionHand.class))
                .add("result_stack", SerializableDataTypes.ITEM_STACK, null)
                .add("held_item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("result_item_action", ApoliDataTypes.ITEM_ACTION, null),
            data ->
                (type, player) -> {
                    return new PreventBeingUsedPower(type, player,
                        (EnumSet<InteractionHand>)data.get("hands"),
                        InteractionResult.FAIL,
                        (Predicate<ItemStack>)data.get("item_condition"),
                        (Consumer<Tuple<Level, ItemStack>>)data.get("held_item_action"),
                        (ItemStack)data.get("result_stack"),
                        (Consumer<Tuple<Level, ItemStack>>)data.get("result_item_action"),
                        (Consumer<Tuple<Entity, Entity>>) data.get("bientity_action"),
                        (ConditionFactory<Tuple<Entity, Entity>>.Instance)data.get("bientity_condition"));
                })
            .allowCondition();
    }
}

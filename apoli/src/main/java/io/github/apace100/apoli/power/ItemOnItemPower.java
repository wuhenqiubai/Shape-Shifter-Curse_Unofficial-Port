package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ItemOnItemPower extends Power {

    private final Predicate<ItemStack> usingItemCondition;
    private final Predicate<ItemStack> onItemCondition;

    private final int resultFromOnStack;
    private final ItemStack newStack;
    private final Consumer<Tuple<Level, ItemStack>> usingItemAction;
    private final Consumer<Tuple<Level, ItemStack>> onItemAction;
    private final Consumer<Tuple<Level, ItemStack>> resultItemAction;
    private final Consumer<Entity> entityAction;

    public ItemOnItemPower(PowerType<?> type, LivingEntity entity, Predicate<ItemStack> usingItemCondition, Predicate<ItemStack> onItemCondition, ItemStack newStack, Consumer<Tuple<Level, ItemStack>> usingItemAction, Consumer<Tuple<Level, ItemStack>> onItemAction, Consumer<Tuple<Level, ItemStack>> resultItemAction, Consumer<Entity> entityAction, int resultFromOnStack) {
        super(type, entity);
        this.usingItemCondition = usingItemCondition;
        this.onItemCondition = onItemCondition;
        this.newStack = newStack;
        this.usingItemAction = usingItemAction;
        this.onItemAction = onItemAction;
        this.resultItemAction = resultItemAction;
        this.entityAction = entityAction;
        this.resultFromOnStack = resultFromOnStack;
    }

    public boolean doesApply(ItemStack using, ItemStack on) {
        if(usingItemCondition != null && !usingItemCondition.test(using)) {
            return false;
        }
        if(onItemCondition != null && !onItemCondition.test(on)) {
            return false;
        }
        return true;
    }

    public ItemStack execute(ItemStack using, ItemStack on, Slot slot) {
        ItemStack stack;
        if(newStack != null) {
            stack = newStack.copy();
            if(resultItemAction != null) {
                resultItemAction.accept(new Tuple<>(entity.level(), stack));
            }
        } else {
            if(resultFromOnStack > 0) {
                stack = on.split(resultFromOnStack);
            } else {
                stack = on;
            }
            if(resultItemAction != null) {
                resultItemAction.accept(new Tuple<>(entity.level(), stack));
            }
        }
        if(usingItemAction != null) {
            usingItemAction.accept(new Tuple<>(entity.level(), using));
        }
        if(onItemAction != null) {
            onItemAction.accept(new Tuple<>(entity.level(), on));
        }
        if(newStack != null || resultItemAction != null) {
            Player player = (Player)entity;
            if(slot.getItem().isEmpty()) {
                slot.set(stack);
            } else {
                player.getInventory().placeItemBackInInventory(stack);
            }
        }
        if(entityAction != null) {
            entityAction.accept(entity);
        }
        return stack;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("item_on_item"),
            new SerializableData()
                .add("using_item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("on_item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("result_from_on_stack", SerializableDataTypes.INT, 0)
                .add("result", SerializableDataTypes.ITEM_STACK, null)
                .add("using_item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("on_item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("result_item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null),
            data ->
                (type, player) -> new ItemOnItemPower(type, player,
                    (ConditionFactory<ItemStack>.Instance)data.get("using_item_condition"),
                    (ConditionFactory<ItemStack>.Instance)data.get("on_item_condition"),
                    (ItemStack)data.get("result"), (ActionFactory<Tuple<Level, ItemStack>>.Instance)data.get("using_item_action"),
                    (ActionFactory<Tuple<Level, ItemStack>>.Instance)data.get("on_item_action"),
                    (ActionFactory<Tuple<Level, ItemStack>>.Instance)data.get("result_item_action"),
                    (ActionFactory<Entity>.Instance)data.get("entity_action"),
                    data.getInt("result_from_on_stack")))
            .allowCondition();
    }
}

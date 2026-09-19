package io.github.apace100.apoli.power;

import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class InteractionPower extends Power {

    private final EnumSet<InteractionHand> hands;
    private final InteractionResult actionResult;
    private final Predicate<ItemStack> itemCondition;
    protected final Consumer<Tuple<Level, ItemStack>> heldItemAction;
    protected final ItemStack itemResult;
    protected final Consumer<Tuple<Level, ItemStack>> resultItemAction;

    public InteractionPower(PowerType<?> type, LivingEntity entity, EnumSet<InteractionHand> hands, InteractionResult actionResult, Predicate<ItemStack> itemCondition, Consumer<Tuple<Level, ItemStack>> heldItemAction, ItemStack itemResult, Consumer<Tuple<Level, ItemStack>> resultItemAction) {
        super(type, entity);
        this.hands = hands;
        this.actionResult = actionResult;
        this.itemCondition = itemCondition;
        this.heldItemAction = heldItemAction;
        this.itemResult = itemResult;
        this.resultItemAction = resultItemAction;
    }

    public boolean shouldExecute(InteractionHand hand, ItemStack heldStack) {
        if(!doesApplyToHand(hand)) {
            return false;
        }
        if(!doesApplyToItem(heldStack)) {
            return false;
        }
        return true;
    }

    public boolean doesApplyToHand(InteractionHand hand) {
        return hands.contains(hand);
    }

    public boolean doesApplyToItem(ItemStack heldStack) {
        return itemCondition == null || itemCondition.test(heldStack);
    }

    public InteractionResult getActionResult() {
        return actionResult;
    }

    protected void performActorItemStuff(InteractionPower power, Player actor, InteractionHand hand) {
        ItemStack heldStack = actor.getItemInHand(hand);
        if(power.heldItemAction != null) {
            power.heldItemAction.accept(new Tuple<>(actor.level(), heldStack));
        }
        ItemStack resultingStack = power.itemResult == null ? heldStack : power.itemResult.copy();
        boolean modified = power.itemResult != null;
        if(power.resultItemAction != null) {
            power.resultItemAction.accept(new Tuple<>(actor.level(), resultingStack));
            modified = true;
        }
        if(modified) {
            if(heldStack.isEmpty()) {
                actor.setItemInHand(hand, resultingStack);
            } else {
                actor.getInventory().placeItemBackInInventory(resultingStack);
            }
        }
    }
}

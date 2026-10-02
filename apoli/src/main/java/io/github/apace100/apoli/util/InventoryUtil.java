package io.github.apace100.apoli.util;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.InventoryPower;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.util.ArgumentWrapper;
import io.github.apace100.calio.util.LazyItemStack;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SlotRanges;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class InventoryUtil {

    public enum InventoryType {
        INVENTORY,
        POWER
    }

    public enum ProcessMode {
        STACKS(stack -> 1),
        ITEMS(ItemStack::getCount);

        private final Function<ItemStack, Integer> processor;

        ProcessMode(Function<ItemStack, Integer> processor) {
            this.processor = processor;
        }

        public Function<ItemStack, Integer> getProcessor() {
            return processor;
        }
    }

    public static Set<Integer> getSlots(SerializableData.Instance data) {

        Set<Integer> slots = new HashSet<>();

        data.<ArgumentWrapper<Integer>>ifPresent("slot", iaw -> slots.add(iaw.get()));
        data.<List<ArgumentWrapper<Integer>>>ifPresent("slots", iaws -> slots.addAll(iaws.stream().map(ArgumentWrapper::get).toList()));

        if (slots.isEmpty()) slots.addAll(SlotRanges.allNames().flatMap(s -> SlotRanges.nameToIds(s).slots().stream()).toList());

        return slots;

    }

    public static int checkInventory(SerializableData.Instance data, Entity entity, InventoryPower inventoryPower, Function<ItemStack, Integer> processor) {

        Predicate<ItemStack> itemCondition = data.get("item_condition");
        Set<Integer> slots = getSlots(data);
        deduplicateSlots(entity, slots);
        int matches = 0;

        if (inventoryPower == null) {
            for (int slot : slots) {

                SlotAccess stackReference = entity.getSlot(slot);
                if (stackReference == null) {
                    continue;
                }

                ItemStack stack = stackReference.get();
                if ((itemCondition == null && !stack.isEmpty()) || (itemCondition == null || itemCondition.test(stack))) {
                    matches += processor.apply(stack);
                }

            }
        }

        else {
            for (int slot : slots) {

                if (slot < 0 || slot >= inventoryPower.getContainerSize()) {
                    continue;
                }

                ItemStack stack = inventoryPower.getItem(slot);
                if ((itemCondition == null && !stack.isEmpty()) || (itemCondition == null || itemCondition.test(stack))) {
                    matches += processor.apply(stack);
                }

            }
        }

        return matches;

    }

    public static void modifyInventory(SerializableData.Instance data, Entity entity, InventoryPower inventoryPower, Function<ItemStack, Integer> processor, int limit) {

        if(limit <= 0) {
            limit = Integer.MAX_VALUE;
        }

        Set<Integer> slots = getSlots(data);
        deduplicateSlots(entity, slots);

        Consumer<Entity> entityAction = data.get("entity_action");
        Predicate<ItemStack> itemCondition = data.get("item_condition");
        ActionFactory<Tuple<Level, ItemStack>>.Instance itemAction = data.get("item_action");

        int counter = 0;

        if (inventoryPower == null) {
            for(int slot : slots) {

                SlotAccess stackReference = entity.getSlot(slot);
                if (stackReference == null) continue;

                ItemStack itemStack = stackReference.get();
                if (itemStack.isEmpty()) continue;

                if (!(itemCondition == null || itemCondition.test(itemStack))) continue;

                if (entityAction != null) entityAction.accept(entity);

                int amount = processor.apply(itemStack);
                for(int i = 0; i < amount; i++) {
                    itemAction.accept(new Tuple<>(entity.level(), itemStack));

                    counter += 1;

                    if(counter >= limit) {
                        break;
                    }
                }

                if(counter >= limit) {
                    break;
                }
            }
        } else {
            slots.removeIf(slot -> slot < 0 || slot >= inventoryPower.getContainerSize());
            for(int slot : slots) {

                ItemStack itemStack = inventoryPower.getItem(slot);
                if (itemStack.isEmpty()) continue;

                if (!(itemCondition == null || itemCondition.test(itemStack))) continue;

                if (entityAction != null) entityAction.accept(entity);

                int amount = processor.apply(itemStack);
                for(int i = 0; i < amount; i++) {
                    itemAction.accept(new Tuple<>(entity.level(), itemStack));

                    counter += 1;

                    if(counter >= limit) {
                        break;
                    }
                }

                if(counter >= limit) {
                    break;
                }
            }
        }

    }



    public static void replaceInventory(SerializableData.Instance data, Entity entity, InventoryPower inventoryPower) {

        Set<Integer> slots = getSlots(data);
        deduplicateSlots(entity, slots);

        Consumer<Entity> entityAction = data.get("entity_action");
        Predicate<ItemStack> itemCondition = data.get("item_condition");
        Consumer<Tuple<Level, ItemStack>> itemAction = data.get("item_action");

        LazyItemStack replacementStack = data.get("stack");
        boolean mergeNbt = data.getBoolean("merge_nbt");

        if (inventoryPower == null) slots.forEach(
            slot -> {

                SlotAccess stackReference = entity.getSlot(slot);
                if (stackReference == null) return;

                ItemStack itemStack = stackReference.get();
                if (!(itemCondition == null || itemCondition.test(itemStack))) return;

                if (entityAction != null) entityAction.accept(entity);

                ItemStack stackAfterReplacement = replacementStack.createStack();
                if (mergeNbt) {
                    itemStack.applyComponents(stackAfterReplacement.getComponents());
                    stackAfterReplacement.applyComponents(itemStack.getComponents());
                }

                stackReference.set(stackAfterReplacement);
                if (itemAction != null) itemAction.accept(new Tuple<>(entity.level(), stackAfterReplacement));

            }
        );

        else {
            slots.removeIf(slot -> slot < 0 || slot >= inventoryPower.getContainerSize());
            slots.forEach(
                slot -> {

                    ItemStack itemStack = inventoryPower.getItem(slot);
                    if (!(itemCondition == null || itemCondition.test(itemStack))) return;

                    if (entityAction != null) entityAction.accept(entity);

                    ItemStack stackAfterReplacement = replacementStack.createStack();
                    if (mergeNbt) {
                        itemStack.applyComponents(stackAfterReplacement.getComponents());
                        stackAfterReplacement.applyComponents(itemStack.getComponents());
                    }

                    inventoryPower.setItem(slot, stackAfterReplacement);
                    if (itemAction != null) itemAction.accept(new Tuple<>(entity.level(), stackAfterReplacement));

                }
            );
        }

    }

    public static void dropInventory(SerializableData.Instance data, Entity entity, InventoryPower inventoryPower) {

        Set<Integer> slots = getSlots(data);
        deduplicateSlots(entity, slots);

        int amount = data.getInt("amount");
        boolean throwRandomly = data.getBoolean("throw_randomly");
        boolean retainOwnership = data.getBoolean("retain_ownership");

        Consumer<Entity> entityAction = data.get("entity_action");
        Predicate<ItemStack> itemCondition = data.get("item_condition");
        Consumer<Tuple<Level, ItemStack>> itemAction = data.get("item_action");

        if (inventoryPower == null) slots.forEach(
            slot -> {

                SlotAccess stackReference = entity.getSlot(slot);
                if (stackReference == null) return;

                ItemStack itemStack = stackReference.get();
                if (itemStack.isEmpty()) return;

                if (!(itemCondition == null || itemCondition.test(itemStack))) return;

                if (entityAction != null) entityAction.accept(entity);
                if (itemAction != null) itemAction.accept(new Tuple<>(entity.level(), itemStack));

                if (amount != 0) {

                    int newAmount = amount < 0 ? amount * -1 : amount;

                    ItemStack droppedStack = itemStack.split(newAmount);
                    throwItem(entity, droppedStack, throwRandomly, retainOwnership);

                    stackReference.set(itemStack);

                }

                else {
                    throwItem(entity, itemStack, throwRandomly, retainOwnership);
                    stackReference.set(ItemStack.EMPTY);
                }

            }
        );

        else {
            slots.removeIf(slot -> slot < 0 || slot >= inventoryPower.getContainerSize());
            slots.forEach(
                slot -> {

                    ItemStack itemStack = inventoryPower.getItem(slot);
                    if (itemStack.isEmpty()) return;

                    if (!(itemCondition == null || itemCondition.test(itemStack))) return;

                    if (entityAction != null) entityAction.accept(entity);
                    if (itemAction != null) itemAction.accept(new Tuple<>(entity.level(), itemStack));

                    if (amount != 0) {

                        int newAmount = amount < 0 ? amount * -1 : amount;

                        ItemStack droppedStack = itemStack.split(newAmount);
                        throwItem(entity, droppedStack, throwRandomly, retainOwnership);

                        inventoryPower.setItem(slot, itemStack);

                    }

                    else {
                        throwItem(entity, itemStack, throwRandomly, retainOwnership);
                        inventoryPower.setItem(slot, ItemStack.EMPTY);
                    }

                }
            );
        }

    }

    public static void throwItem(Entity thrower, ItemStack itemStack, boolean throwRandomly, boolean retainOwnership) {

        if (itemStack.isEmpty()) return;
        if (thrower instanceof Player playerEntity && playerEntity.level().isClientSide()) playerEntity.swing(InteractionHand.MAIN_HAND);

        double yOffset = thrower.getEyeY() - 0.30000001192092896D;
        ItemEntity itemEntity = new ItemEntity(thrower.level(), thrower.getX(), yOffset, thrower.getZ(), itemStack);
        itemEntity.setPickUpDelay(40);

        Random random = new Random();

        float f;
        float g;

        if (retainOwnership) itemEntity.setThrower(thrower);
        if (throwRandomly) {
            f = random.nextFloat() * 0.5F;
            g = random.nextFloat() * 6.2831855F;
            itemEntity.setDeltaMovement(- Mth.sin(g) * f, 0.20000000298023224D, Mth.cos(g) * f);
        }
        else {
            f = 0.3F;
            g = Mth.sin(thrower.getXRot() * 0.017453292F);
            float h = Mth.cos(thrower.getXRot() * 0.017453292F);
            float i = Mth.sin(thrower.getYRot() * 0.017453292F);
            float j = Mth.cos(thrower.getYRot() * 0.017453292F);
            float k = random.nextFloat() * 6.2831855F;
            float l = 0.02F * random.nextFloat();
            itemEntity.setDeltaMovement(
                (double) (- i * h * f) + Math.cos(k) * (double) l,
                (-g * f + 0.1F + (random.nextFloat() - random.nextFloat()) * 0.1F),
                (double) (j * h * f) + Math.sin(k) * (double) l
            );
        }

        thrower.level().addFreshEntity(itemEntity);

    }

    public static void forEachStack(Entity entity, Consumer<ItemStack> itemStackConsumer) {
        SlotRanges.allNames().flatMapToInt(s -> SlotRanges.nameToIds(s).slots().intStream()).distinct().forEach(slot -> {
            SlotAccess stackReference = entity.getSlot(slot);
            if (stackReference != null) {
                ItemStack itemStack = stackReference.get();
                if (!itemStack.isEmpty()) {
                    itemStackConsumer.accept(itemStack);
                }
            }
        });

        Optional<PowerHolderComponent> optionalPowerHolderComponent = PowerHolderComponent.KEY.maybeGet(entity);
        if(optionalPowerHolderComponent.isPresent()) {
            PowerHolderComponent phc = optionalPowerHolderComponent.get();
            List<InventoryPower> inventoryPowers = phc.getPowers(InventoryPower.class);
            for(InventoryPower inventoryPower : inventoryPowers) {
                for(int index = 0; index < inventoryPower.getContainerSize(); index++) {
                    ItemStack stack = inventoryPower.getItem(index);
                    if(stack.isEmpty()) {
                        continue;
                    }
                    itemStackConsumer.accept(stack);
                }
            }
        }
    }

    private static void deduplicateSlots(Entity entity, Set<Integer> slots) {
        if(entity instanceof Player player) {
            int selectedSlot = player.getInventory().getSelectedSlot();
            Integer hotbarSlot = SlotRanges.nameToIds("hotbar." + selectedSlot).slots().getInt(0);
            if(slots.contains(hotbarSlot)) {
                Integer mainHandSlot = SlotRanges.nameToIds("weapon.mainhand").slots().getInt(0);
                slots.remove(mainHandSlot);
            }
        }
    }
}

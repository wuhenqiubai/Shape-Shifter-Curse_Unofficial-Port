package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class StartingEquipmentPower extends Power {

    private final List<ItemStack> itemStacks = new LinkedList<>();
    private final HashMap<Integer, ItemStack> slottedStacks = new HashMap<>();
    private boolean recurrent;

    public StartingEquipmentPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }

    public void setRecurrent(boolean recurrent) {
        this.recurrent = recurrent;
    }

    public void addStack(ItemStack stack) {
        this.itemStacks.add(stack);
    }

    public void addStack(int slot, ItemStack stack) {
        slottedStacks.put(slot, stack);
    }

    @Override
    public void onGained() {
        giveStacks();
    }

    @Override
    public void onRespawn() {
        if(recurrent) {
            giveStacks();
        }
    }

    private void giveStacks() {
        slottedStacks.forEach((slot, stack) -> {
            if(entity instanceof Player) {
                Player player = (Player)entity;
                Inventory inventory = player.getInventory();
                if(inventory.getItem(slot).isEmpty()) {
                    inventory.setItem(slot, stack);
                } else {
                    player.addItem(stack);
                }
            } else if (!entity.level().isClientSide()) {
                entity.spawnAtLocation(stack);
            }
        });
        itemStacks.forEach(is -> {
            ItemStack copy = is.copy();
            if(entity instanceof Player) {
                Player player = (Player)entity;
                player.addItem(copy);
            } else if (!entity.level().isClientSide()) {
                entity.spawnAtLocation(copy);
            }
        });
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("starting_equipment"),
            new SerializableData()
                .add("stack", ApoliDataTypes.POSITIONED_ITEM_STACK, null)
                .add("stacks", ApoliDataTypes.POSITIONED_ITEM_STACKS, null)
                .add("recurrent", SerializableDataTypes.BOOLEAN, false),
            data ->
                (type, player) -> {
                    StartingEquipmentPower power = new StartingEquipmentPower(type, player);
                    if(data.isPresent("stack")) {
                        Tuple<Integer, ItemStack> stack = (Tuple<Integer, ItemStack>)data.get("stack");
                        int slot = stack.getA();
                        if(slot > Integer.MIN_VALUE) {
                            power.addStack(stack.getA(), stack.getB());
                        } else {
                            power.addStack(stack.getB());
                        }
                    }
                    if(data.isPresent("stacks")) {
                        ((List<Tuple<Integer, ItemStack>>)data.get("stacks"))
                            .forEach(integerItemStackPair -> {
                                int slot = integerItemStackPair.getA();
                                if(slot > Integer.MIN_VALUE) {
                                    power.addStack(integerItemStackPair.getA(), integerItemStackPair.getB());
                                } else {
                                    power.addStack(integerItemStackPair.getB());
                                }
                            });
                    }
                    power.setRecurrent(data.getBoolean("recurrent"));
                    return power;
                });
    }
}

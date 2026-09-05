package net.onixary.shapeShifterCurseFabric.util.Accessory;

import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DefaultAccessory {
    static {
        AccessoryUtils.registerAccessoryMod("trinkets", new AccessoryUtils.AccessoryIO() {
            @Override
            public int priority() {
                return 1000;
            }

            @Override
            public boolean canLoaded() {
                return FabricLoader.getInstance().isModLoaded("trinkets");
            }

            @Override
            public Map<Tuple<@Nullable String, String>, List<ItemStack>> getEntitySlots(LivingEntity entity) {
                Map<Tuple<@Nullable String, String>, List<ItemStack>> map = new HashMap<>();
                Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(entity);
                if (component.isEmpty()) {
                    return map;
                }
                Map<String, Map<String, TrinketInventory>> invMap = component.get().getInventory();
                for (String slotGroup : invMap.keySet()) {
                    for (String slotName : invMap.get(slotGroup).keySet()) {
                        List<ItemStack> stacks = new ArrayList<>();
                        TrinketInventory inventory = invMap.get(slotGroup).get(slotName);
                        for (int i = 0; i < inventory.getContainerSize(); i++) {
                            ItemStack stack = inventory.getItem(i);
                            stacks.add(stack);
                        }
                        map.put(new Tuple<>(slotGroup, slotName), stacks);
                    }
                }
                return map;
            }

            @Override
            public List<ItemStack> getEntitySlot(LivingEntity entity, @Nullable String SlotGroup, String SlotName) {
                List<ItemStack> ItemList = new ArrayList<>();
                if (SlotGroup == null) {
                    return ItemList;
                }
                Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(entity);
                if (component.isEmpty()) {
                    return ItemList;
                }
                Map<String, Map<String, TrinketInventory>> invMap = component.get().getInventory();
                if (invMap.containsKey(SlotGroup) && invMap.get(SlotGroup).containsKey(SlotName)) {
                    TrinketInventory inventory = invMap.get(SlotGroup).get(SlotName);
                    for (int i = 0; i < inventory.getContainerSize(); i++) {
                        ItemStack stack = inventory.getItem(i);
                        ItemList.add(stack);
                    }
                }
                return ItemList;
            }

            @Override
            public @Nullable ItemStack getEntitySlot(LivingEntity entity, @Nullable String SlotGroup, String SlotName, int Index) {
                if (SlotGroup == null) {
                    return null;
                }
                Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(entity);
                if (component.isEmpty()) {
                    return null;
                }
                Map<String, Map<String, TrinketInventory>> invMap = component.get().getInventory();
                if (invMap.containsKey(SlotGroup) && invMap.get(SlotGroup).containsKey(SlotName)) {
                    TrinketInventory inventory = invMap.get(SlotGroup).get(SlotName);
                    if (Index >= 0 && Index < inventory.getContainerSize()) {
                        return inventory.getItem(Index);
                    }
                }
                return null;
            }

            @Override
            public void setEntitySlot(LivingEntity entity, @Nullable String SlotGroup, String SlotName, int Index, ItemStack stack) {
                if (SlotGroup == null) {
                    return;
                }
                Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(entity);
                if (component.isEmpty()) {
                    return;
                }
                Map<String, Map<String, TrinketInventory>> invMap = component.get().getInventory();
                if (invMap.containsKey(SlotGroup) && invMap.get(SlotGroup).containsKey(SlotName)) {
                    TrinketInventory inventory = invMap.get(SlotGroup).get(SlotName);
                    inventory.setItem(Index, stack);
                }
            }
        });

        AccessoryUtils.reCalcAccessoryMod();
    }

    public static void init() {
        // DO NOTHING
    }
}

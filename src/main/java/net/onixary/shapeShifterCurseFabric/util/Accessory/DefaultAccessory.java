package net.onixary.shapeShifterCurseFabric.util.Accessory;

import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketInventory;
import eu.pb4.trinkets.api.TrinketsApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                // 4.0: getTrinketComponent 返回 Optional<TrinketComponent>，
                // 现改为 getAttachment 直接返回非空的 TrinketAttachment（不再有「无配件组件」分支）。
                // 注：getInventory() 在 4.0 已标 @Deprecated(forRemoval)，但仍是维护中的实时视图
                //（impl 里整体重建、值就是活的 TrinketInventory），setEntitySlot 的写回照常生效。
                // 后续 Trinkets 移除它时再迁到 getGroups()/getInventory(slotId)。
                TrinketAttachment attachment = TrinketsApi.getAttachment(entity);
                Map<String, Map<String, TrinketInventory>> invMap = attachment.getInventory();
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
                TrinketAttachment attachment = TrinketsApi.getAttachment(entity);
                Map<String, Map<String, TrinketInventory>> invMap = attachment.getInventory();
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
                TrinketAttachment attachment = TrinketsApi.getAttachment(entity);
                Map<String, Map<String, TrinketInventory>> invMap = attachment.getInventory();
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
                TrinketAttachment attachment = TrinketsApi.getAttachment(entity);
                Map<String, Map<String, TrinketInventory>> invMap = attachment.getInventory();
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

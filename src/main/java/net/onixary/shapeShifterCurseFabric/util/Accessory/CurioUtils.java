package net.onixary.shapeShifterCurseFabric.util.Accessory;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Curios 兼容工具（NeoForge 版，重写自上游 Forge 预编译 CurioUtilsImpl.class 的 @Overwrite 逻辑）。
 * 供 DefaultAccessory 的 curio AccessoryIO 读写 Curios 槽位。NeoForge 的 getCuriosInventory 返回 Optional。
 */
public class CurioUtils {
    public static boolean isEquipped(LivingEntity entity, Item item) {
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler == null) {
            return false;
        }
        for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
            IDynamicStackHandler stacks = stacksHandler.getStacks();
            for (int i = 0; i < stacks.getSlots(); i++) {
                if (stacks.getStackInSlot(i).is(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Map<String, List<ItemStack>> getEntitySlots(LivingEntity entity) {
        Map<String, List<ItemStack>> map = new HashMap<>();
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler == null) {
            return map;
        }
        for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
            IDynamicStackHandler stacks = entry.getValue().getStacks();
            List<ItemStack> list = new ArrayList<>();
            for (int i = 0; i < stacks.getSlots(); i++) {
                list.add(stacks.getStackInSlot(i));
            }
            map.put(entry.getKey(), list);
        }
        return map;
    }

    public static List<ItemStack> getEntitySlot(LivingEntity entity, String SlotName) {
        List<ItemStack> list = new ArrayList<>();
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler == null) {
            return list;
        }
        ICurioStacksHandler stacksHandler = handler.getCurios().get(SlotName);
        if (stacksHandler != null) {
            IDynamicStackHandler stacks = stacksHandler.getStacks();
            for (int i = 0; i < stacks.getSlots(); i++) {
                list.add(stacks.getStackInSlot(i));
            }
        }
        return list;
    }

    public static void setEntitySlot(LivingEntity entity, String SlotName, int Index, ItemStack stack) {
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler == null) {
            return;
        }
        ICurioStacksHandler stacksHandler = handler.getCurios().get(SlotName);
        if (stacksHandler != null && Index >= 0 && Index < stacksHandler.getStacks().getSlots()) {
            stacksHandler.getStacks().setStackInSlot(Index, stack);
        }
    }
}

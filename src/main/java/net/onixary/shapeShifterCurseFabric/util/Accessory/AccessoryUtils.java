package net.onixary.shapeShifterCurseFabric.util.Accessory;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.event.SSCEvent;
import net.onixary.shapeShifterCurseFabric.items.accessory.AccessoryItem;
import net.onixary.shapeShifterCurseFabric.util.TrinketUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccessoryUtils {
    public interface AccessoryIO {
        public default int priority() {
            return 1000;
        }

        public default boolean canLoaded() {
            return true;
        }

        public Map<Tuple<@Nullable String, String>, List<ItemStack>> getEntitySlots(LivingEntity entity);

        public List<@Nullable ItemStack> getEntitySlot(LivingEntity entity, @Nullable String SlotGroup, String SlotName);

        public @Nullable ItemStack getEntitySlot(LivingEntity entity, @Nullable String SlotGroup, String SlotName, int Index);

        public void setEntitySlot(LivingEntity entity, @Nullable String SlotGroup, String SlotName, int Index, ItemStack stack);
    }

    public static void onPlayerEquip(Player player, ResourceLocation itemID, String pluginID) {
        if (BuiltInRegistries.ITEM.get(itemID) instanceof AccessoryItem && !nowAccessoryModID.equals(pluginID)) {
            return;
        }
        TrinketUtils.ApplyAccessoryPowerOnEquip(player, itemID);
        SSCEvent.ACCESSORY_EQUIP.invoker().onEvent(player, itemID, pluginID);
    }

    public static void onPlayerUnEquip(Player player, ResourceLocation itemID, String pluginID) {
        if (BuiltInRegistries.ITEM.get(itemID) instanceof AccessoryItem && !nowAccessoryModID.equals(pluginID)) {
            return;
        }
        TrinketUtils.ApplyAccessoryPowerOnUnEquip(player, itemID);
        SSCEvent.ACCESSORY_UNEQUIP.invoker().onEvent(player, itemID, pluginID);
    }

    public static boolean CanAutoExecute(ResourceLocation itemID, String pluginID) {
        return TrinketUtils.getAccessoryMixinAuto(itemID);
    }

    public static void registerAccessoryMod(String identifier, AccessoryIO io) {
        accessoryModInterfaces.put(identifier, io);
    }

    public static void reCalcAccessoryMod() {
        nowAccessoryModID = "";
        nowAccessoryMod = null;
        activeAccessoryModInterfaces.clear();
        if (accessoryModInterfaces.isEmpty()) {
            return;
        }
        List<Tuple<AccessoryIO, Integer>> list = new ArrayList<>();
        for (Map.Entry<String, AccessoryIO> entry : accessoryModInterfaces.entrySet()) {
            if (entry.getValue().canLoaded()) {
                list.add(new Tuple<>(entry.getValue(), entry.getValue().priority()));
                activeAccessoryModInterfaces.put(entry.getKey(), entry.getValue());
            } else {
                ShapeShifterCurseFabric.LOGGER.warn("Accessory Mod: " + entry.getKey() + " can't loaded");
            }
        }
        list.sort((o1, o2) -> o2.getB() - o1.getB());
        if (!list.isEmpty()) {
            nowAccessoryMod = list.get(0).getA();
        }
        for (Map.Entry<String, AccessoryIO> entry : accessoryModInterfaces.entrySet()) {
            if (entry.getValue() == nowAccessoryMod) {
                nowAccessoryModID = entry.getKey();
                ShapeShifterCurseFabric.LOGGER.info("Active Accessory IO: " + entry.getKey());
            }
        }
    }

    public static void onStartServer() {
        if (nowAccessoryMod == null) {
            // 可能有其他的兼容方法 所以不会强制弹RuntimeException
            ShapeShifterCurseFabric.LOGGER.warn("Can't find any active Accessory IO. Accessory features may not work");
        }
    }

    public static Map<String, AccessoryIO> accessoryModInterfaces = new HashMap<>();
    public static Map<String, AccessoryIO> activeAccessoryModInterfaces = new HashMap<>();
    public static String nowAccessoryModID = "";
    public static AccessoryIO nowAccessoryMod = null;

    public static @Nullable Map<Tuple<@Nullable String, String>, List<ItemStack>> getEntitySlots(LivingEntity entity, @Nullable String accessoryModID) {
        if (nowAccessoryMod == null) {
            return null;
        }
        if (accessoryModID == null || accessoryModID.equals("auto")) {
            return nowAccessoryMod.getEntitySlots(entity);
        }
        if (accessoryModInterfaces.containsKey(accessoryModID)) {
            return accessoryModInterfaces.get(accessoryModID).getEntitySlots(entity);
        }
        ShapeShifterCurseFabric.LOGGER.warn("Can't find Accessory Mod: " + accessoryModID);
        return null;
    }

    public static @Nullable List<ItemStack> getEntitySlot(LivingEntity entity, @Nullable String accessoryModID, String SlotGroup, String SlotName) {
        if (nowAccessoryMod == null) {
            return null;
        }
        if (accessoryModID == null || accessoryModID.equals("auto")) {
            return nowAccessoryMod.getEntitySlot(entity, SlotGroup, SlotName);
        }
        if (accessoryModInterfaces.containsKey(accessoryModID)) {
            return accessoryModInterfaces.get(accessoryModID).getEntitySlot(entity, SlotGroup, SlotName);
        }
        ShapeShifterCurseFabric.LOGGER.warn("Can't find Accessory Mod: " + accessoryModID);
        return null;
    }

    public static @Nullable ItemStack getEntitySlot(LivingEntity entity, @Nullable String accessoryModID, String SlotGroup, String SlotName, int Index) {
        if (nowAccessoryMod == null) {
            return null;
        }
        if (accessoryModID == null || accessoryModID.equals("auto")) {
            return nowAccessoryMod.getEntitySlot(entity, SlotGroup, SlotName, Index);
        }
        if (accessoryModInterfaces.containsKey(accessoryModID)) {
            return accessoryModInterfaces.get(accessoryModID).getEntitySlot(entity, SlotGroup, SlotName, Index);
        }
        ShapeShifterCurseFabric.LOGGER.warn("Can't find Accessory Mod: " + accessoryModID);
        return null;
    }

    public static void setEntitySlot(LivingEntity entity, @Nullable String accessoryModID, String SlotGroup, String SlotName, int Index, ItemStack stack) {
        if (nowAccessoryMod == null) {
            return;
        }
        if (accessoryModID == null || accessoryModID.equals("auto")) {
            nowAccessoryMod.setEntitySlot(entity, SlotGroup, SlotName, Index, stack);
        } else if (accessoryModInterfaces.containsKey(accessoryModID)) {
            accessoryModInterfaces.get(accessoryModID).setEntitySlot(entity, SlotGroup, SlotName, Index, stack);
        } else {
            ShapeShifterCurseFabric.LOGGER.warn("Can't find Accessory Mod: " + accessoryModID);
        }
    }
}

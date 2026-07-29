package net.onixary.shapeShifterCurseFabric.util;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.additional_power.CustomEdiblePower;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CustomEdibleUtils {

    public static HashMap<UUID, HashMap<Identifier, FoodProperties>> customEdibleMap = new HashMap<>();

    public static FoodProperties getPowerFoodComponent(Player user, ItemStack itemStack) {
        if (user == null || itemStack == null || itemStack.isEmpty()) {
            return null;
        }
        HashMap<Identifier, FoodProperties> customEdible = customEdibleMap.computeIfAbsent(user.getUUID(), k -> new HashMap<>());
        return customEdible.getOrDefault(BuiltInRegistries.ITEM.getKey(itemStack.getItem()), null);
    }

    public static void addCustomEdible(Player user, Identifier itemId, FoodProperties foodComponent) {
        HashMap<Identifier, FoodProperties> customEdible = customEdibleMap.computeIfAbsent(user.getUUID(), k -> new HashMap<>());
        customEdible.put(itemId, foodComponent);
    }

    public static void addCustomEdibleWithList(Player user, List<Identifier> itemIdList, FoodProperties foodComponent) {
        HashMap<Identifier, FoodProperties> customEdible = customEdibleMap.computeIfAbsent(user.getUUID(), k -> new HashMap<>());
        for (Identifier itemId : itemIdList) {
            customEdible.put(itemId, foodComponent);
        }
    }

    public static void clearCustomEdible(Player user, Identifier itemId) {
        if (customEdibleMap.containsKey(user.getUUID())) {
            customEdibleMap.get(user.getUUID()).remove(itemId);
        }
    }

    public static void clearCustomEdibleWithList(Player user, List<Identifier> itemIdList) {
        if (customEdibleMap.containsKey(user.getUUID())) {
            HashMap<Identifier, FoodProperties> customEdible = customEdibleMap.get(user.getUUID());
            for (Identifier itemId : itemIdList) {
                customEdible.remove(itemId);
            }
        }
    }

    public static void ReloadPlayerCustomEdible(Player user) {
        try {
            customEdibleMap.computeIfAbsent(user.getUUID(), k -> new HashMap<>()).clear();
            HashMap<Identifier, FoodProperties> customEdible = customEdibleMap.get(user.getUUID());
            PowerHolderComponent.getPowers(user, CustomEdiblePower.class).forEach(
                    customEdiblePower -> {
                        for (Identifier itemId : customEdiblePower.getItemIdList()) {
                            customEdible.put(itemId, customEdiblePower.getFoodComponent());
                        }
                    }
            );
        } catch (Exception e) {
            // ShapeShifterCurseFabric.LOGGER.error("Reload Player Custom Edible Failed", e);
        }
    }

}
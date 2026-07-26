package net.onixary.shapeShifterCurseFabric.util;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.additional_power.CustomEdiblePower;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CustomEdibleUtils {

    public static HashMap<UUID, HashMap<ResourceLocation, FoodProperties>> customEdibleMap = new HashMap<>();

    public static FoodProperties getPowerFoodComponent(Player user, ItemStack itemStack) {
        if (user == null || itemStack == null || itemStack.isEmpty()) {
            return null;
        }
        HashMap<ResourceLocation, FoodProperties> customEdible = customEdibleMap.computeIfAbsent(user.getUUID(), k -> new HashMap<>());
        return customEdible.getOrDefault(BuiltInRegistries.ITEM.getKey(itemStack.getItem()), null);
    }

    public static void addCustomEdible(Player user, ResourceLocation itemId, FoodProperties foodComponent) {
        HashMap<ResourceLocation, FoodProperties> customEdible = customEdibleMap.computeIfAbsent(user.getUUID(), k -> new HashMap<>());
        customEdible.put(itemId, foodComponent);
    }

    public static void addCustomEdibleWithList(Player user, List<ResourceLocation> itemIdList, FoodProperties foodComponent) {
        HashMap<ResourceLocation, FoodProperties> customEdible = customEdibleMap.computeIfAbsent(user.getUUID(), k -> new HashMap<>());
        for (ResourceLocation itemId : itemIdList) {
            customEdible.put(itemId, foodComponent);
        }
    }

    public static void clearCustomEdible(Player user, ResourceLocation itemId) {
        if (customEdibleMap.containsKey(user.getUUID())) {
            customEdibleMap.get(user.getUUID()).remove(itemId);
        }
    }

    public static void clearCustomEdibleWithList(Player user, List<ResourceLocation> itemIdList) {
        if (customEdibleMap.containsKey(user.getUUID())) {
            HashMap<ResourceLocation, FoodProperties> customEdible = customEdibleMap.get(user.getUUID());
            for (ResourceLocation itemId : itemIdList) {
                customEdible.remove(itemId);
            }
        }
    }

    public static void ReloadPlayerCustomEdible(Player user) {
        try {
            customEdibleMap.computeIfAbsent(user.getUUID(), k -> new HashMap<>()).clear();
            HashMap<ResourceLocation, FoodProperties> customEdible = customEdibleMap.get(user.getUUID());
            PowerHolderComponent.getPowers(user, CustomEdiblePower.class).forEach(
                    customEdiblePower -> {
                        for (ResourceLocation itemId : customEdiblePower.getItemIdList()) {
                            customEdible.put(itemId, customEdiblePower.getFoodComponent());
                        }
                    }
            );
        } catch (Exception e) {
            // ShapeShifterCurseFabric.LOGGER.error("Reload Player Custom Edible Failed", e);
        }
    }

}

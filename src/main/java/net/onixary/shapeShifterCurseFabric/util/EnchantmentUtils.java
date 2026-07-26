package net.onixary.shapeShifterCurseFabric.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.onixary.shapeShifterCurseFabric.items.tools.DiamondMiningClaw;

import java.util.HashMap;
import java.util.HashSet;

public class EnchantmentUtils {
    public static  HashMap<ResourceKey<Enchantment>, HashSet<Class<? extends Item>>> enchantmentItemClassMap = new HashMap<>();
    public static HashMap<ResourceKey<Enchantment>, HashSet<ResourceLocation>> enchantmentItemIDMap = new HashMap<>();

    static {
        registerEnchantmentItem(Enchantments.SHARPNESS, DiamondMiningClaw.class);
        registerEnchantmentItem(Enchantments.SMITE, DiamondMiningClaw.class);
        registerEnchantmentItem(Enchantments.BANE_OF_ARTHROPODS, DiamondMiningClaw.class);
        registerEnchantmentItem(Enchantments.FIRE_ASPECT, DiamondMiningClaw.class);
        registerEnchantmentItem(Enchantments.KNOCKBACK, DiamondMiningClaw.class);
        registerEnchantmentItem(Enchantments.LOOTING, DiamondMiningClaw.class);
    }

    public static void registerEnchantmentItem(ResourceKey<Enchantment> enchantment, Class<? extends Item> itemClass) {
        enchantmentItemClassMap.computeIfAbsent(enchantment, k -> new HashSet<>()).add(itemClass);
    }

    public static void registerEnchantmentItem(ResourceKey<Enchantment> enchantment, ResourceLocation itemID) {
        enchantmentItemIDMap.computeIfAbsent(enchantment, k -> new HashSet<>()).add(itemID);
    }

    public static void registerEnchantmentItem(ResourceKey<Enchantment> enchantment, Item item) {
        enchantmentItemIDMap.computeIfAbsent(enchantment, k -> new HashSet<>()).add(BuiltInRegistries.ITEM.getKey(item));
    }

    public static boolean isItemCanEnchantment(ResourceKey<Enchantment> enchantment, ItemStack itemStack) {
        if (enchantmentItemClassMap.containsKey(enchantment)) {
            for (Class<? extends Item> itemClass : enchantmentItemClassMap.get(enchantment)) {
                if (itemClass.isInstance(itemStack.getItem())) {
                    return true;
                }
            }
        }
        if (enchantmentItemIDMap.containsKey(enchantment)) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
            for (ResourceLocation itemID : enchantmentItemIDMap.get(enchantment)) {
                if (itemID.equals(id)) {
                    return true;
                }
            }
        }
        return false;
    }
}

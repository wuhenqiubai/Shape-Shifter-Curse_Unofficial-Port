package net.onixary.shapeShifterCurseFabric.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;

public class FeralRenderUtils {
    public final static HashSet<ResourceLocation> FeralMouthItemBlackList = new HashSet<>();
    static {
        FeralMouthItemBlackList.add(ResourceLocation.fromNamespaceAndPath("tacz", "modern_kinetic_gun"));
    }

    public static boolean isFeralMouthItemBlackListed(ResourceLocation identifier) {
        return FeralMouthItemBlackList.contains(identifier);
    }

    public static boolean isFeralMouthItemBlackListed(ItemStack itemStack) {
        try {
            return FeralMouthItemBlackList.contains(BuiltInRegistries.ITEM.getKey(itemStack.getItem()));
        } catch (Exception e) {
            return false;
        }
    }
}
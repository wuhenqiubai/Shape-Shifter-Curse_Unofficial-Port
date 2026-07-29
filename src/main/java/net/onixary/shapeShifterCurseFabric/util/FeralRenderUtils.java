package net.onixary.shapeShifterCurseFabric.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;

public class FeralRenderUtils {
    public final static HashSet<Identifier> FeralMouthItemBlackList = new HashSet<>();
    static {
        FeralMouthItemBlackList.add(Identifier.fromNamespaceAndPath("tacz", "modern_kinetic_gun"));
    }

    public static boolean isFeralMouthItemBlackListed(Identifier identifier) {
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
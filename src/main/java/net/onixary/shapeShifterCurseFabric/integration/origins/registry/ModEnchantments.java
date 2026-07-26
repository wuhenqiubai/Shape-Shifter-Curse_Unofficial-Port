package net.onixary.shapeShifterCurseFabric.integration.origins.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;

public class ModEnchantments {

    public static final ResourceKey<Enchantment> WATER_PROTECTION =
        ResourceKey.create(Registries.ENCHANTMENT, Origins.identifier("water_protection"));

    public static void register() {
        // Enchantment registration is handled by JSON data files in 1.21.
        // No Java registration needed.
    }

//    private static Enchantment register(String path, Enchantment enchantment) {
//        Registry.register(Registries.ENCHANTMENT, new Identifier(Origins.MODID, path), enchantment);
//        return enchantment;
//    }
}
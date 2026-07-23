package net.onixary.shapeShifterCurseFabric.integration.origins.registry;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;

public class ModEnchantments {

    public static final RegistryKey<Enchantment> WATER_PROTECTION =
        RegistryKey.of(RegistryKeys.ENCHANTMENT, Origins.identifier("water_protection"));

    public static void register() {
        // Enchantment registration is handled by JSON data files in 1.21.
        // No Java registration needed.
    }

//    private static Enchantment register(String path, Enchantment enchantment) {
//        Registry.register(Registries.ENCHANTMENT, new Identifier(Origins.MODID, path), enchantment);
//        return enchantment;
//    }
}
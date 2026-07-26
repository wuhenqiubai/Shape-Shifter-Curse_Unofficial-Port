package net.onixary.shapeShifterCurseFabric.items.armors;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import java.util.List;
import java.util.Map;

public class NetheriteMorphscaleArmorMaterial {
    public static final ArmorMaterial INSTANCE = new ArmorMaterial(
        Map.of(
            ArmorItem.Type.HELMET, 3,
            ArmorItem.Type.CHESTPLATE, 6,
            ArmorItem.Type.LEGGINGS, 7,
            ArmorItem.Type.BOOTS, 3
        ),
        15,
        SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE,
        () -> Ingredient.ofItems(Items.NETHERITE_SCRAP),
        List.of(new ArmorMaterial.Layer(Identifier.of(ShapeShifterCurseFabric.MOD_ID, "netherite"))),
        2.0F,
        0.1F
    );

    public static final RegistryEntry<ArmorMaterial> ENTRY = Registry.registerReference(Registries.ARMOR_MATERIAL,
            Identifier.of(ShapeShifterCurseFabric.MOD_ID, "netherite_morphscale"), INSTANCE);
}

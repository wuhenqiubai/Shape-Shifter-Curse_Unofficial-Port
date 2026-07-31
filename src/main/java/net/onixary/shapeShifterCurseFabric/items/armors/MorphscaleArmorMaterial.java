package net.onixary.shapeShifterCurseFabric.items.armors;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.Map;

public class MorphscaleArmorMaterial {
    public static final ArmorMaterial INSTANCE = new ArmorMaterial(
        33,  // durability
        Map.of(
            ArmorType.HELMET, 2,
            ArmorType.CHESTPLATE, 6,
            ArmorType.LEGGINGS, 7,
            ArmorType.BOOTS, 2
        ),
        10,  // enchantmentValue
        SoundEvents.ARMOR_EQUIP_DIAMOND,
        1.0F,  // toughness
        0.0F,  // knockbackResistance
        ItemTags.REPAIRS_DIAMOND_ARMOR,
        EquipmentAssets.DIAMOND
    );
}

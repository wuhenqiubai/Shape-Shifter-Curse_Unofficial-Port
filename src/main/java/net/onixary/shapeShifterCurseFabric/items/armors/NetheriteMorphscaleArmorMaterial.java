package net.onixary.shapeShifterCurseFabric.items.armors;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.Map;

public class NetheriteMorphscaleArmorMaterial {
    public static final ArmorMaterial INSTANCE = new ArmorMaterial(
        37,  // durability
        Map.of(
            ArmorType.HELMET, 3,
            ArmorType.CHESTPLATE, 6,
            ArmorType.LEGGINGS, 7,
            ArmorType.BOOTS, 3
        ),
        15,  // enchantmentValue
        SoundEvents.ARMOR_EQUIP_NETHERITE,
        2.0F,  // toughness
        0.1F,  // knockbackResistance
        ItemTags.REPAIRS_NETHERITE_ARMOR,
        EquipmentAssets.NETHERITE
    );
}

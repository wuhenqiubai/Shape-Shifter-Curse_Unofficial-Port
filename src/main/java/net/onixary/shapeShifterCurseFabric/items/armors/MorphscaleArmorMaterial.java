package net.onixary.shapeShifterCurseFabric.items.armors;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import java.util.List;
import java.util.Map;

public class MorphscaleArmorMaterial {
    public static final ArmorMaterial INSTANCE = new ArmorMaterial(
        Map.of(
            ArmorItem.Type.HELMET, 2,
            ArmorItem.Type.CHESTPLATE, 6,
            ArmorItem.Type.LEGGINGS, 7,
            ArmorItem.Type.BOOTS, 2
        ),
        10,
        SoundEvents.ARMOR_EQUIP_DIAMOND,
        () -> Ingredient.of(Items.DIAMOND),
        List.of(new ArmorMaterial.Layer(Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "diamond"))),
        1.0F,
        0.0F
    );

    public static final Holder<ArmorMaterial> ENTRY = Registry.registerForHolder(BuiltInRegistries.ARMOR_MATERIAL,
            Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "morphscale"), INSTANCE);
}
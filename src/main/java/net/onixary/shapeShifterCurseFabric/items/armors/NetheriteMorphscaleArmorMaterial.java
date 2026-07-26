package net.onixary.shapeShifterCurseFabric.items.armors;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
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
        SoundEvents.ARMOR_EQUIP_NETHERITE,
        () -> Ingredient.of(Items.NETHERITE_SCRAP),
        List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "netherite"))),
        2.0F,
        0.1F
    );

    public static final Holder<ArmorMaterial> ENTRY = Registry.registerForHolder(BuiltInRegistries.ARMOR_MATERIAL,
            ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "netherite_morphscale"), INSTANCE);
}

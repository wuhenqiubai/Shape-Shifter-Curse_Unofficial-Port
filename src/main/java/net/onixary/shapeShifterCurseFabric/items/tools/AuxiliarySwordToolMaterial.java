package net.onixary.shapeShifterCurseFabric.items.tools;

import net.minecraft.block.Block;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

public class AuxiliarySwordToolMaterial implements ToolMaterial {
    public static final AuxiliarySwordToolMaterial INSTANCE = new AuxiliarySwordToolMaterial();

    @Override
    public int getDurability() {
        return 781;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return 1f;
    }

    @Override
    public float getAttackDamage() {
        return 4;
    }

    @Override
    public TagKey<Block> getInverseTag() {
        return BlockTags.INCORRECT_FOR_WOODEN_TOOL;
    }

    @Override
    public int getEnchantability() {
        return 0;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.ofItems(new ItemConvertible[]{Items.DIAMOND});
    }
}

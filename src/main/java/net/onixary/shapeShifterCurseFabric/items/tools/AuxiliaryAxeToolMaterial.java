package net.onixary.shapeShifterCurseFabric.items.tools;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public class AuxiliaryAxeToolMaterial implements Tier {
    public static final AuxiliaryAxeToolMaterial INSTANCE = new AuxiliaryAxeToolMaterial();

    @Override
    public int getUses() {
        return 781;
    }

    @Override
    public float getSpeed() {
        return 1f;
    }

    @Override
    public float getAttackDamageBonus() {
        return 6;
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return BlockTags.INCORRECT_FOR_WOODEN_TOOL;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(new ItemLike[]{Items.DIAMOND});
    }
}

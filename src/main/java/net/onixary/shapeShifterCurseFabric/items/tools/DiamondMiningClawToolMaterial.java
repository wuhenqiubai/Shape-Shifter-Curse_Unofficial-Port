package net.onixary.shapeShifterCurseFabric.items.tools;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public class DiamondMiningClawToolMaterial implements Tier {
    public static final DiamondMiningClawToolMaterial INSTANCE = new DiamondMiningClawToolMaterial();

    @Override
    public int getUses() {
        return 781;
    }

    @Override
    public float getSpeed() {
        return 4f;   // 石稿速度  蝙蝠为 4 + (4 * 2) = 12  4/3倍下界合金镐
    }

    @Override
    public float getAttackDamageBonus() {
        return 2;
    }

    @Override
    public int getEnchantmentValue() {
        return 10;
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

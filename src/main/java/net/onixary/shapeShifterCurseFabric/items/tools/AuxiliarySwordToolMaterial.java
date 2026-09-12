package net.onixary.shapeShifterCurseFabric.items.tools;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class AuxiliarySwordToolMaterial implements Tier {
    public static final AuxiliarySwordToolMaterial INSTANCE = new AuxiliarySwordToolMaterial();

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
        return 4;
    }

    @Override
    public @NotNull TagKey<Block> getIncorrectBlocksForDrops() {
        return BlockTags.INCORRECT_FOR_WOODEN_TOOL;
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public @NotNull Ingredient getRepairIngredient() {
        return Ingredient.of(new ItemLike[]{Items.DIAMOND});
    }
}

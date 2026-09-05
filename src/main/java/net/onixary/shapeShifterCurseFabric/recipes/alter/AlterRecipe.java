package net.onixary.shapeShifterCurseFabric.recipes.alter;

import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.onixary.shapeShifterCurseFabric.recipes.RecipeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AlterRecipe implements Recipe<RecipeInput> {

    @Override
    public @NotNull RecipeType<?> getType() {
        return RecipeUtils.ALTER_RECIPE;
    }

    public abstract int recipeTime();

    // 进度锁 虽然SSC目前没这个需求 但我的拓展有这个需求
    public boolean canCraft(@Nullable Player player) {
        return true;
    }

    // 可以做到一个配方 消耗N个物品
    public boolean InputsCountEnough(WorldlyContainer inventory) {
        return true;
    }

    public void consumeInputs(WorldlyContainer inventory) {
        for (int i = 0; i < 9; i++) {
            inventory.getItem(i).shrink(1);
        }
    }

    public List<ItemStack> getExtraOutput(WorldlyContainer inventory) {
        return List.of();
    }
}
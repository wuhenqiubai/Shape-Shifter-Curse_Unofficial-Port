package net.onixary.shapeShifterCurseFabric.recipes.altar;

import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.onixary.shapeShifterCurseFabric.recipes.RecipeUtils;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;

public abstract class AltarRecipe implements Recipe<RecipeInput> {

    // 26.1: Recipe#group() 从 default 方法变成了抽象方法，所有实现类必须提供。
    // SSC 的祭坛配方没有分组概念，沿用旧 default 的空串（配方书里不分栏）。
    @Override
    public String group() {
        return "";
    }

    // 26.1: Recipe#showNotification() 同样由 default(true) 变为抽象，沿用旧默认行为。
    @Override
    public boolean showNotification() {
        return true;
    }

    @Override
    public @NonNull RecipeType<? extends Recipe<RecipeInput>> getType() {
        return RecipeUtils.Altar_RECIPE;
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

    public int fuelUsage() {
        return 1;
    }
}
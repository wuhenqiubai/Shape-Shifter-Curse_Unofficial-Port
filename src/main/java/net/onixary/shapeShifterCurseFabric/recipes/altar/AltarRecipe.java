package net.onixary.shapeShifterCurseFabric.recipes.altar;

import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.onixary.shapeShifterCurseFabric.recipes.RecipeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

// 这个AI写的什么玩意啊 和那个EyeScale一样 给我看的血压飙升 之后得重构(不用之后了 现在就得重构)
public abstract class AltarRecipe implements Recipe<RecipeInput> {
    @Override
    public @NotNull RecipeType<? extends Recipe<RecipeInput>> getType() {
        return RecipeUtils.ALTAR_RECIPE;
    }

    @Override
    public boolean isSpecial() {
        return true;
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
            ItemStack input = inventory.getItem(i);
            ItemStack remainder = inputRemainder(input);
            input.shrink(1);
            if (input.isEmpty() && !remainder.isEmpty()) {
                inventory.setItem(i, remainder);
            }
        }
    }

    public List<ItemStack> getExtraOutput(WorldlyContainer inventory) {
        List<ItemStack> remainders = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            ItemStack input = inventory.getItem(i);
            ItemStack remainder = inputRemainder(input);
            if (input.getCount() > 1 && !remainder.isEmpty()) {
                remainders.add(remainder);
            }
        }
        return remainders;
    }

    public static ItemStack inputRemainder(ItemStack input) {
        // Unlike fluid buckets, vanilla's powder snow bucket declares no recipe remainder.
        if (input.is(Items.POWDER_SNOW_BUCKET)) {
            return new ItemStack(Items.BUCKET);
        }
        return input.getItem().getRecipeRemainder(input);
    }

    public int fuelUsage() {
        return 1;
    }

    // 辅助函数 不可重载 除非修改了燃料消耗逻辑
    public int totalFuelUsage() {
        return fuelUsage() * recipeTime();
    }
}

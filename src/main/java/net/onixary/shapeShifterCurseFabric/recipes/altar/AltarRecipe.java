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

public abstract class AltarRecipe implements Recipe<RecipeInput> {
    // Optional exact fuel budget, in fuel units (one moondust = 800).
    // -1 preserves existing datapacks' per-tick fuel_cost behavior.
    public int totalFuelCost = -1;

    public int totalFuelUsage() {
        return totalFuelCost >= 0 ? totalFuelCost : fuelUsage() * recipeTime();
    }

    public int fuelUsage(int progress) {
        if (totalFuelCost < 0) {
            return fuelUsage();
        }
        // Spread the remainder over the recipe without rounding away any fuel.
        return (int) (((long) (progress + 1) * totalFuelCost / recipeTime())
                - ((long) progress * totalFuelCost / recipeTime()));
    }

    @Override
    public @NotNull RecipeType<? extends Recipe<RecipeInput>> getType() {
        return RecipeUtils.ALTAR_RECIPE;
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

    private static ItemStack inputRemainder(ItemStack input) {
        // Unlike fluid buckets, vanilla's powder snow bucket declares no recipe remainder.
        if (input.is(Items.POWDER_SNOW_BUCKET)) {
            return new ItemStack(Items.BUCKET);
        }
        // 1.21.11: Item.hasCraftingRemainingItem() 已移除，Item.getCraftingRemainder() 直接返回 ItemStack；
        // 这里沿用 Fabric API 的 stack-aware 版本 FabricItem#getRecipeRemainder(ItemStack)（无剩余物时返回 EMPTY）。
        return input.getItem().getRecipeRemainder(input);
    }

    public int fuelUsage() {
        return 1;
    }
}

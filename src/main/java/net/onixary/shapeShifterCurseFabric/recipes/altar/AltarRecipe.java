package net.onixary.shapeShifterCurseFabric.recipes.altar;

import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
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
        // 26.1: 1.21.11 侧沿用的 Fabric API 方法 FabricItem#getRecipeRemainder(ItemStack) 已被移除。
        // 原版 Item#getCraftingRemainder() 现在直接返回 @Nullable ItemStackTemplate ——
        // 无剩余物时返回 null，正好对应旧行为的 ItemStack.EMPTY。
        ItemStackTemplate remainder = input.getItem().getCraftingRemainder();
        return remainder == null ? ItemStack.EMPTY : remainder.create();
    }

    public int fuelUsage() {
        return 1;
    }
}

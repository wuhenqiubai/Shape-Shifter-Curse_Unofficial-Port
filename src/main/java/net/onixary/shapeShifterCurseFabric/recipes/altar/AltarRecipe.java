package net.onixary.shapeShifterCurseFabric.recipes.altar;

import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.onixary.shapeShifterCurseFabric.recipes.RecipeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;

public abstract class AltarRecipe implements Recipe<RecipeInput> {

    @Override
    public @NotNull RecipeType<? extends Recipe<RecipeInput>> getType() {
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

    // 消耗 0~9 全部槽位：0-8 是输入格，9 是催化剂槽。
    // ⚠ 原先只扫 0~8，漏掉 slot 9 → 催化剂（钻石 / 下界之星）永不消耗，可无限复用。
    // 另：必须先判空再 shrink——空格是共享单例 ItemStack.EMPTY，直接 shrink 会把它写成 count=-1
    //（isEmpty() 因 this == EMPTY 短路才没当场出问题，但 getCount() 已污染）。
    public void consumeInputs(WorldlyContainer inventory) {
        for (int i = 0; i <= 9; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                stack.shrink(1);
            }
        }
    }

    public List<ItemStack> getExtraOutput(WorldlyContainer inventory) {
        return List.of();
    }

    public int fuelUsage() {
        return 1;
    }
}
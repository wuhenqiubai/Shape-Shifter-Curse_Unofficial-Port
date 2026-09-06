package net.onixary.shapeShifterCurseFabric.recipes.alter;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Alter 祭坛的 RecipeInput。
 * <p>
 * 槽位语义与上游 1.20.1 一致：slot 0-8 为 3x3 键材，slot 9 为燃料/催化剂槽（二者共用），slot 10 为输出。
 * 1.21.1 把 {@code matches(Recipe, Level)} 参数从 SidedInventory 改成 RecipeInput，但 ({@code CraftingInput.of(3, 3, ...)})
 * 的 {@code ofPositioned} 会按非空网格裁剪列表并丢弃超出 3x3 的格子——非满 3x3 时 slot 9 被丢弃，
 * 导致 {@code matches()} 里 {@code recipeInput.getItem(9)} 校验 catalyst 时 {@link IndexOutOfBoundsException}。
 * <p>
 * 故自定义一个不裁剪、线性映射 inventory 0-9 的 RecipeInput。AlterBlockEntity 自身不 implements RecipeInput，
 * 以避免与 {@code WorldlyContainer#getItem/isEmpty} 双接口在 remap 时二义。
 */
public class AlterRecipeInput implements RecipeInput {
    public static final int SIZE = 10;

    private final List<ItemStack> items; // size 10: 0-8 = 3x3 键材, 9 = 燃料/催化剂

    public AlterRecipeInput(@NotNull List<ItemStack> items) {
        if (items.size() < SIZE) {
            throw new IllegalArgumentException("AlterRecipeInput needs at least " + SIZE + " slots, got " + items.size());
        }
        this.items = List.copyOf(items.subList(0, SIZE));
    }

    @Override
    public @NotNull ItemStack getItem(int i) {
        return this.items.get(i);
    }

    @Override
    public int size() {
        return this.items.size();
    }
}

package net.onixary.shapeShifterCurseFabric.recipes.altar;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.onixary.shapeShifterCurseFabric.blocks.block_entity.AltarBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Altar 祭坛的 RecipeInput。
 * <p>
 * 槽位语义与上游 1.20.1 一致：slot 0-8 为 3x3 键材，slot 9 为燃料/催化剂槽（二者共用），slot 10 为输出。
 * 1.21.1 把 {@code matches(Recipe, Level)} 参数从 SidedInventory 改成 RecipeInput，但 ({@code CraftingInput.of(3, 3, ...)})
 * 的 {@code ofPositioned} 会按非空网格裁剪列表并丢弃超出 3x3 的格子——非满 3x3 时 slot 9 被丢弃，
 * 导致 {@code matches()} 里 {@code recipeInput.getItem(9)} 校验 catalyst 时 {@link IndexOutOfBoundsException}。
 * <p>
 * 故自定义一个不裁剪、线性映射 inventory 0-9 的 RecipeInput。AltarBlockEntity 自身不 implements RecipeInput，
 * 以避免与 {@code WorldlyContainer#getItem/isEmpty} 双接口在 remap 时二义。
 *
 * <p><b>⚠️ 合并提示：</b>本类在我方 1.21.1 分支上是必需的（上游 1.20.1 用 SidedInventory 不需要它）。
 * 合并 upstream 的 altar 改名时容易整个文件被漏掉，导致 {@code AltarBlockEntity.craftInput()} 找不到符号。</p>
 */
public class AltarRecipeInput implements RecipeInput {
    public static final int SIZE = 10;

    private final List<ItemStack> items; // size 10: 0-8 = 3x3 键材, 9 = 燃料/催化剂

    /**
     * 产出本输入的那个祭坛方块实体。
     *
     * <p>存在的理由：{@code BuiltinAltarRecipe}（硬编码的内置配方，Apoli/饰品升级用）需要拿到 BE
     * 本身才能跑它的运行时匹配/产出函数，而 BE 刻意没有 implements {@code RecipeInput}（见类注释），
     * 于是那边写的 {@code recipeInput instanceof AltarBlockEntity} 恒为 false、内置配方全部失效。
     * 改为让本类反向持有 owner，供其取回。</p>
     *
     * <p>可为 null：仅当调用方没有 BE 上下文时（例如测试或纯数据校验）才会如此。</p>
     */
    private final @Nullable AltarBlockEntity owner;

    public AltarRecipeInput(@NotNull List<ItemStack> items) {
        this(items, null);
    }

    public AltarRecipeInput(@NotNull List<ItemStack> items, @Nullable AltarBlockEntity owner) {
        if (items.size() < SIZE) {
            throw new IllegalArgumentException("AltarRecipeInput needs at least " + SIZE + " slots, got " + items.size());
        }
        this.items = List.copyOf(items.subList(0, SIZE));
        this.owner = owner;
    }

    /** 产出本输入的祭坛方块实体；无 BE 上下文时为 {@code null}。 */
    public @Nullable AltarBlockEntity owner() {
        return this.owner;
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

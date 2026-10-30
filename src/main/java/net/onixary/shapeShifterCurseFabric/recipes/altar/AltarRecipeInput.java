package net.onixary.shapeShifterCurseFabric.recipes.altar;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.onixary.shapeShifterCurseFabric.blocks.block_entity.AltarBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AltarRecipeInput implements RecipeInput {
    public static final int SIZE = 10;

    private final List<ItemStack> items;

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

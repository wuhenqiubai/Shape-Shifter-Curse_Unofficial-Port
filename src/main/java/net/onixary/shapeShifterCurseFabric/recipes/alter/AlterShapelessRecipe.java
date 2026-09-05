package net.onixary.shapeShifterCurseFabric.recipes.alter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.recipes.RecipeSerializerRegister;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class AlterShapelessRecipe extends AlterRecipe {
    public final String group;
    public final CraftingBookCategory category;
    public final ItemStack result;
    public final List<Ingredient> input;
    public final int recipeTime;
    private @Nullable PlacementInfo placementInfo;

    public AlterShapelessRecipe(String group, CraftingBookCategory category, ItemStack result, List<Ingredient> input, int recipeTime) {
        this.group = group;
        this.category = category;
        this.result = result;
        this.input = input;
        this.recipeTime = recipeTime;
    }

    @Override
    public int recipeTime() {
        return recipeTime;
    }

    // 进度锁
    @Override
    public boolean canCraft(@Nullable Player player) {
        return true;
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.create(this.input);
        }
        return this.placementInfo;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return switch (this.category) {
            case BUILDING -> RecipeBookCategories.CRAFTING_BUILDING_BLOCKS;
            case EQUIPMENT -> RecipeBookCategories.CRAFTING_EQUIPMENT;
            case REDSTONE -> RecipeBookCategories.CRAFTING_REDSTONE;
            case MISC -> RecipeBookCategories.CRAFTING_MISC;
        };
    }

    @Override
    public boolean matches(RecipeInput recipeInput, Level level) {
        int count = 0;
        StackedItemContents contents = new StackedItemContents();
        for (int j = 0; j < recipeInput.size(); ++j) {
            ItemStack itemStack = recipeInput.getItem(j);
            if (!itemStack.isEmpty()) {
                ++count;
                contents.accountStack(itemStack, 1);
            }
        }
        if (count != this.input.size()) {
            return false;
        }
        return contents.canCraft(this, null);
    }

    @Override
    public @NonNull ItemStack assemble(@NonNull RecipeInput recipeInput, HolderLookup.@NonNull Provider provider) {
        return this.result.copy();
    }

    @Override
    public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return RecipeSerializerRegister.ALTER_SHAPELESS_RECIPE;
    }

    public static class Serializer implements RecipeSerializer<AlterShapelessRecipe> {
        private static final MapCodec<AlterShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
                CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC).forGetter(r -> r.category),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.result),
                Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(r -> r.input),
                Codec.INT.optionalFieldOf("time", 200).forGetter(r -> r.recipeTime)
            ).apply(instance, AlterShapelessRecipe::new)
        );
        private static final StreamCodec<RegistryFriendlyByteBuf, AlterShapelessRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, r -> r.group,
            CraftingBookCategory.STREAM_CODEC, r -> r.category,
            ItemStack.STREAM_CODEC, r -> r.result,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), r -> r.input,
            ByteBufCodecs.VAR_INT, r -> r.recipeTime,
            AlterShapelessRecipe::new
        );

        @Override
        public @NonNull MapCodec<AlterShapelessRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NonNull StreamCodec<RegistryFriendlyByteBuf, AlterShapelessRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}

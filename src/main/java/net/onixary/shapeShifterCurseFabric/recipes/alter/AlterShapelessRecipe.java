package net.onixary.shapeShifterCurseFabric.recipes.alter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.recipes.RecipeSerializerRegister;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class AlterShapelessRecipe extends AlterRecipe {
    public final String group;
    public final CraftingBookCategory category;
    public final ItemStack result;
    public final NonNullList<Ingredient> input;
    public final int recipeTime;

    public AlterShapelessRecipe(String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> input, int recipeTime) {
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
    public boolean matches(RecipeInput recipeInput, Level level) {
        StackedContents recipeMatcher = new StackedContents();
        int i = 0;

        for(int j = 0; j < 9; ++j) {
            ItemStack itemStack = recipeInput.getItem(j);
            if (!itemStack.isEmpty()) {
                ++i;
                recipeMatcher.accountStack(itemStack, 1);
            }
        }
        return i == this.input.size() && recipeMatcher.canCraft(this, (IntList) null);
    }

    @Override
    public @NonNull ItemStack assemble(@NonNull RecipeInput recipeInput, HolderLookup.@NonNull Provider provider) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= this.input.size();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return this.result;
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
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap(
                    list -> {
                        Ingredient[] arr = list.stream().filter(i -> !i.isEmpty()).toArray(Ingredient[]::new);
                        if (arr.length == 0) {
                            return DataResult.error(() -> "No ingredients for alter shapeless recipe");
                        }
                        if (arr.length > 9) {
                            return DataResult.error(() -> "Too many ingredients for alter shapeless recipe");
                        }
                        return DataResult.success(NonNullList.of(Ingredient.EMPTY, arr));
                    }, DataResult::success)
                    .forGetter(r -> r.input),
                Codec.INT.optionalFieldOf("time", 200).forGetter(r -> r.recipeTime)
            ).apply(instance, AlterShapelessRecipe::new)
        );
        private static final StreamCodec<RegistryFriendlyByteBuf, AlterShapelessRecipe> STREAM_CODEC = StreamCodec.of(
            Serializer::toNetwork, Serializer::fromNetwork
        );

        @Override
        public @NonNull MapCodec<AlterShapelessRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NonNull StreamCodec<RegistryFriendlyByteBuf, AlterShapelessRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static AlterShapelessRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            String group = buf.readUtf();
            CraftingBookCategory category = buf.readEnum(CraftingBookCategory.class);
            int n = buf.readVarInt();
            NonNullList<Ingredient> list = NonNullList.withSize(n, Ingredient.EMPTY);
            list.replaceAll(i -> Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
            ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
            int time = buf.readVarInt();
            return new AlterShapelessRecipe(group, category, result, list, time);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, AlterShapelessRecipe r) {
            buf.writeUtf(r.group);
            buf.writeEnum(r.category);
            buf.writeVarInt(r.input.size());
            for (Ingredient ingredient : r.input) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient);
            }
            ItemStack.STREAM_CODEC.encode(buf, r.result);
            buf.writeVarInt(r.recipeTime);
        }
    }
}

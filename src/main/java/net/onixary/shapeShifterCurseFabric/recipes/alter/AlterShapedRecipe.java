package net.onixary.shapeShifterCurseFabric.recipes.alter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.recipes.RecipeSerializerRegister;
import org.jetbrains.annotations.Nullable;

public class AlterShapedRecipe extends AlterRecipe {
    public final String group;
    public final CraftingBookCategory category;
    public final ShapedRecipePattern pattern;
    public final ItemStack result;
    public final int recipeTime;

    public AlterShapedRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result, int recipeTime) {
        this.group = group;
        this.category = category;
        this.pattern = pattern;
        this.result = result;
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

    private boolean matchesPattern(RecipeInput inv, int offsetX, int offsetY, boolean flipped) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                int k = i - offsetX;
                int l = j - offsetY;
                Ingredient ingredient = Ingredient.EMPTY;
                if (k >= 0 && l >= 0 && k < this.pattern.width() && l < this.pattern.height()) {
                    if (flipped) {
                        ingredient = this.pattern.ingredients().get(this.pattern.width() - k - 1 + l * this.pattern.width());
                    } else {
                        ingredient = this.pattern.ingredients().get(k + l * this.pattern.width());
                    }
                }
                if (!ingredient.test(inv.getItem(i + j * 3))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean matches(RecipeInput recipeInput, Level level) {
        for (int i = 0; i <= 3 - this.pattern.width(); ++i) {
            for (int j = 0; j <= 3 - this.pattern.height(); ++j) {
                if (this.matchesPattern(recipeInput, i, j, true)) {
                    return true;
                }
                if (this.matchesPattern(recipeInput, i, j, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput recipeInput, HolderLookup.Provider provider) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= this.pattern.width() && height >= this.pattern.height();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return this.result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializerRegister.ALTER_SHAPED_RECIPE;
    }

    public static class Serializer implements RecipeSerializer<AlterShapedRecipe> {
        private static final MapCodec<AlterShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
                CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC).forGetter(r -> r.category),
                ShapedRecipePattern.MAP_CODEC.forGetter(r -> r.pattern),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.result),
                Codec.INT.optionalFieldOf("time", 200).forGetter(r -> r.recipeTime)
            ).apply(instance, AlterShapedRecipe::new)
        );
        private static final StreamCodec<RegistryFriendlyByteBuf, AlterShapedRecipe> STREAM_CODEC = StreamCodec.of(
            Serializer::toNetwork, Serializer::fromNetwork
        );

        @Override
        public MapCodec<AlterShapedRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AlterShapedRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static AlterShapedRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            String group = buf.readUtf();
            CraftingBookCategory category = buf.readEnum(CraftingBookCategory.class);
            ShapedRecipePattern pattern = ShapedRecipePattern.STREAM_CODEC.decode(buf);
            ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
            int time = buf.readVarInt();
            return new AlterShapedRecipe(group, category, pattern, result, time);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, AlterShapedRecipe r) {
            buf.writeUtf(r.group);
            buf.writeEnum(r.category);
            ShapedRecipePattern.STREAM_CODEC.encode(buf, r.pattern);
            ItemStack.STREAM_CODEC.encode(buf, r.result);
            buf.writeVarInt(r.recipeTime);
        }
    }
}

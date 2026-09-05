package net.onixary.shapeShifterCurseFabric.recipes.alter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.recipes.RecipeSerializerRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AlterShapelessRecipe extends AlterRecipe {
    public final ItemStack output;
    public final NonNullList<Ingredient> input;
    public final @Nullable Ingredient catalyst;
    public final int recipeTime;
    public final int fuelCostPerTick;

    public AlterShapelessRecipe(ItemStack output, NonNullList<Ingredient> input, @Nullable Ingredient catalyst, int recipeTime, int fuelCostPerTick) {
        this.output = output;
        this.input = input;
        this.catalyst = catalyst;
        this.recipeTime = recipeTime;
        this.fuelCostPerTick = fuelCostPerTick;
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
    public boolean matches(RecipeInput recipeInput, Level world) {
        if (this.catalyst != null) {
            ItemStack itemStack = recipeInput.getItem(9);
            if (!this.catalyst.test(itemStack)) {
                return false;
            }
        }

        StackedContents recipeMatcher = new StackedContents();
        int i = 0;
        for (int j = 0; j < 9; ++j) {
            ItemStack itemStack = recipeInput.getItem(j);
            if (!itemStack.isEmpty()) {
                ++i;
                recipeMatcher.accountStack(itemStack, 1);
            }
        }
        return i == this.input.size() && recipeMatcher.canCraft(this, null);
    }

    // [1.21.1 修复] Recipe.getIngredients() 默认返回空 NonNullList，StackedContents.canCraft 会读空 ingredients →
    // shapeless 配方匹配必失败。改为返回 input。
    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return this.input;
    }

    @Override
    public int fuelUsage() {
        return fuelCostPerTick;
    }

    @Override
    public @NotNull ItemStack assemble(RecipeInput recipeInput, HolderLookup.Provider provider) {
        return this.output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= this.input.size();
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.Provider provider) {
        return this.output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializerRegister.ALTER_SHAPELESS_RECIPE;
    }

    public static class Serializer implements RecipeSerializer<AlterShapelessRecipe> {
        private static final MapCodec<AlterShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.output),
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
                Ingredient.CODEC_NONEMPTY.optionalFieldOf("catalyst").forGetter(r -> Optional.ofNullable(r.catalyst)),
                Codec.INT.optionalFieldOf("time", 200).forGetter(r -> r.recipeTime),
                Codec.INT.optionalFieldOf("fuel_cost", 1).forGetter(r -> r.fuelCostPerTick)
            ).apply(instance, (output, input, catalyst, time, fuelCost) -> new AlterShapelessRecipe(output, input, catalyst.orElse(null), time, fuelCost))
        );

        private static final StreamCodec<RegistryFriendlyByteBuf, AlterShapelessRecipe> STREAM_CODEC = StreamCodec.of(
            Serializer::toNetwork, Serializer::fromNetwork
        );

        @Override
        public MapCodec<AlterShapelessRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AlterShapelessRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static AlterShapelessRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            Ingredient catalyst = null;
            if (buf.readBoolean()) {
                catalyst = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            }
            int n = buf.readVarInt();
            NonNullList<Ingredient> list = NonNullList.withSize(n, Ingredient.EMPTY);
            list.replaceAll(i -> Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
            ItemStack output = ItemStack.STREAM_CODEC.decode(buf);
            int time = buf.readVarInt();
            int fuelCost = buf.readVarInt();
            return new AlterShapelessRecipe(output, list, catalyst, time, fuelCost);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, AlterShapelessRecipe r) {
            if (r.catalyst != null) {
                buf.writeBoolean(true);
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, r.catalyst);
            } else {
                buf.writeBoolean(false);
            }
            buf.writeVarInt(r.input.size());
            for (Ingredient ingredient : r.input) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient);
            }
            ItemStack.STREAM_CODEC.encode(buf, r.output);
            buf.writeVarInt(r.recipeTime);
            buf.writeVarInt(r.fuelCostPerTick);
        }
    }
}

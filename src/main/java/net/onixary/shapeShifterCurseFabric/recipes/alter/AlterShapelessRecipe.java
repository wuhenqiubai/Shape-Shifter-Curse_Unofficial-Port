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
import java.util.Optional;

// 注：merge 时该文件被误解决（丢了 catalyst/fuelCostPerTick/output，拼成 1.21.11 骨架+1.21.1 网络残骸）。
// 这里按 1.21.1 语义整体复原，并用 1.21.11 接口（PlacementInfo/recipeBookCategory/StackedItemContents/Ingredient.CODEC）。
public class AlterShapelessRecipe extends AlterRecipe {
    public final ItemStack output;
    public final List<Ingredient> input;
    public final @Nullable Ingredient catalyst;
    public final int recipeTime;
    public final int fuelCostPerTick;
    private @Nullable PlacementInfo placementInfo;

    public AlterShapelessRecipe(ItemStack output, List<Ingredient> input, @Nullable Ingredient catalyst, int recipeTime, int fuelCostPerTick) {
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
    public @NonNull PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.create(this.input);
        }
        return this.placementInfo;
    }

    // 1.21.11 Recipe 接口新增；alter 配方无分类概念（json 无 category/group），固定 MISC
    @Override
    public @NonNull RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public boolean matches(@NonNull RecipeInput recipeInput, @NonNull Level level) {
        if (this.catalyst != null) {
            ItemStack itemStack = recipeInput.getItem(9);
            if (!this.catalyst.test(itemStack)) {
                return false;
            }
        }

        StackedItemContents contents = new StackedItemContents();
        int i = 0;
        for (int j = 0; j < 9; ++j) {
            ItemStack itemStack = recipeInput.getItem(j);
            if (!itemStack.isEmpty()) {
                ++i;
                contents.accountStack(itemStack, 1);
            }
        }
        return i == this.input.size() && contents.canCraft(this, null);
    }

    // 1.21.1 修复点：shapeless 必须返回 fuelCostPerTick，否则 fuel_cost 配置不生效
    @Override
    public int fuelUsage() {
        return fuelCostPerTick;
    }

    @Override
    public @NonNull ItemStack assemble(@NonNull RecipeInput recipeInput, HolderLookup.@NonNull Provider provider) {
        return this.output.copy();
    }

    @Override
    public @NonNull RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return RecipeSerializerRegister.ALTER_SHAPELESS_RECIPE;
    }

    public static class Serializer implements RecipeSerializer<AlterShapelessRecipe> {
        private static final MapCodec<AlterShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.output),
                Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(r -> r.input),
                Ingredient.CODEC.optionalFieldOf("catalyst").forGetter(r -> Optional.ofNullable(r.catalyst)),
                Codec.INT.optionalFieldOf("time", 200).forGetter(r -> r.recipeTime),
                Codec.INT.optionalFieldOf("fuel_cost", 1).forGetter(r -> r.fuelCostPerTick)
            ).apply(instance, (output, input, catalyst, time, fuelCost) ->
                new AlterShapelessRecipe(output, input, catalyst.orElse(null), time, fuelCost))
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
            Ingredient catalyst = null;
            if (buf.readBoolean()) {
                catalyst = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            }
            List<Ingredient> list = Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
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
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, r.input);
            ItemStack.STREAM_CODEC.encode(buf, r.output);
            buf.writeVarInt(r.recipeTime);
            buf.writeVarInt(r.fuelCostPerTick);
        }
    }
}

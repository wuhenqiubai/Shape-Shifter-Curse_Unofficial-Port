package net.onixary.shapeShifterCurseFabric.recipes.alter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.recipes.RecipeSerializerRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class AlterShapedRecipe extends AlterRecipe {
    public final ShapedRecipePattern pattern;
    public final ItemStack output;
    public final @Nullable Ingredient catalyst;
    public final int recipeTime;
    public final int fuelCostPerTick;
    public final @Nullable Identifier requireAdvancement;
    private @Nullable PlacementInfo placementInfo;

    public AlterShapedRecipe(ShapedRecipePattern pattern, ItemStack output, @Nullable Ingredient catalyst, int recipeTime, int fuelCostPerTick, @Nullable Identifier requireAdvancement) {
        this.pattern = pattern;
        this.output = output;
        this.catalyst = catalyst;
        this.recipeTime = recipeTime;
        this.fuelCostPerTick = fuelCostPerTick;
        this.requireAdvancement = requireAdvancement;
    }

    @Override
    public int recipeTime() {
        return recipeTime;
    }

    // 进度锁：require_advancement 未完成则不可合成
    @Override
    public boolean canCraft(@Nullable Player player) {
        if (requireAdvancement == null) {
            return true;
        }
        if (player instanceof ServerPlayer playerEntity) {
            MinecraftServer server = playerEntity.level().getServer();
            AdvancementHolder advancement = server.getAdvancements().get(requireAdvancement);
            if (advancement == null) {
                return false;
            }
            AdvancementProgress advancementProgress = playerEntity.getAdvancements().getOrStartProgress(advancement);
            return advancementProgress.isDone();
        }
        return false;
    }

    @Override
    public @NonNull PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.createFromOptionals(this.pattern.ingredients());
        }
        return this.placementInfo;
    }

    @Override
    public @NonNull RecipeBookCategory recipeBookCategory() {
        // 该类无 category 字段（1.21.1 版）；alter 配方无分类概念，固定 MISC
        return RecipeBookCategories.CRAFTING_MISC;
    }

    private boolean matchesPattern(RecipeInput inv, int offsetX, int offsetY, boolean flipped) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                int k = i - offsetX;
                int l = j - offsetY;
                Optional<Ingredient> ingredient = Optional.empty();
                if (k >= 0 && l >= 0 && k < this.pattern.width() && l < this.pattern.height()) {
                    if (flipped) {
                        ingredient = this.pattern.ingredients().get(this.pattern.width() - k - 1 + l * this.pattern.width());
                    } else {
                        ingredient = this.pattern.ingredients().get(k + l * this.pattern.width());
                    }
                }
                if (!Ingredient.testOptionalIngredient(ingredient, inv.getItem(i + j * 3))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean matches(@NonNull RecipeInput recipeInput, @NonNull Level world) {
        if (this.catalyst != null) {
            ItemStack itemStack = recipeInput.getItem(9);
            if (!this.catalyst.test(itemStack)) {
                return false;
            }
        }

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
    public int fuelUsage() {
        return fuelCostPerTick;
    }

    @Override
    public @NotNull ItemStack assemble(@NonNull RecipeInput recipeInput, HolderLookup.@NonNull Provider provider) {
        return this.output.copy();
    }

    @Override
    public @NonNull RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return RecipeSerializerRegister.ALTER_SHAPED_RECIPE;
    }

    public static class Serializer implements RecipeSerializer<AlterShapedRecipe> {
        private static final MapCodec<AlterShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                ShapedRecipePattern.MAP_CODEC.forGetter(r -> r.pattern),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.output),
                Ingredient.CODEC.optionalFieldOf("catalyst").forGetter(r -> Optional.ofNullable(r.catalyst)),
                Codec.INT.optionalFieldOf("time", 200).forGetter(r -> r.recipeTime),
                Codec.INT.optionalFieldOf("fuel_cost", 1).forGetter(r -> r.fuelCostPerTick),
                Identifier.CODEC.optionalFieldOf("require_advancement").forGetter(r -> Optional.ofNullable(r.requireAdvancement))
            ).apply(instance, (pattern, output, catalyst, time, fuelCost, requireAdvancement) ->
                new AlterShapedRecipe(pattern, output, catalyst.orElse(null), time, fuelCost, requireAdvancement.orElse(null)))
        );

        private static final StreamCodec<RegistryFriendlyByteBuf, AlterShapedRecipe> STREAM_CODEC = StreamCodec.of(
            Serializer::toNetwork, Serializer::fromNetwork
        );

        @Override
        public @NotNull MapCodec<AlterShapedRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, AlterShapedRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static AlterShapedRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            Ingredient catalyst = null;
            if (buf.readBoolean()) {
                catalyst = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            }
            Identifier requireAdvancement = null;
            if (buf.readBoolean()) {
                requireAdvancement = Identifier.STREAM_CODEC.decode(buf);
            }
            ShapedRecipePattern pattern = ShapedRecipePattern.STREAM_CODEC.decode(buf);
            ItemStack output = ItemStack.STREAM_CODEC.decode(buf);
            int time = buf.readVarInt();
            int fuelCost = buf.readVarInt();
            return new AlterShapedRecipe(pattern, output, catalyst, time, fuelCost, requireAdvancement);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, AlterShapedRecipe r) {
            if (r.catalyst != null) {
                buf.writeBoolean(true);
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, r.catalyst);
            } else {
                buf.writeBoolean(false);
            }
            if (r.requireAdvancement != null) {
                buf.writeBoolean(true);
                Identifier.STREAM_CODEC.encode(buf, r.requireAdvancement);
            } else {
                buf.writeBoolean(false);
            }
            ShapedRecipePattern.STREAM_CODEC.encode(buf, r.pattern);
            ItemStack.STREAM_CODEC.encode(buf, r.output);
            buf.writeVarInt(r.recipeTime);
            buf.writeVarInt(r.fuelCostPerTick);
        }
    }
}

package net.onixary.shapeShifterCurseFabric.recipes.alter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
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

// 1.21.1 复原：RecipeSerializer 只有 codec()/streamCodec()，上游 1.20 手写 read(JsonObject)/write 无法用。
// 采用语义层方案：字段用 ShapedRecipePattern（含 pattern/key Data，codec 完整），保留 catalyst/fuel_cost/time/
// requireAdvancement 进度锁 + 全部匹配/合成逻辑；id 由 RecipeHolder.id() 管理，recipe 不再自带 id。
public class AlterShapedRecipe extends AlterRecipe {
    public final ShapedRecipePattern pattern;
    public final ItemStack output;
    public final @Nullable Ingredient catalyst;
    public final int recipeTime;
    public final int fuelCostPerTick;
    public final @Nullable ResourceLocation requireAdvancement;

    public AlterShapedRecipe(ShapedRecipePattern pattern, ItemStack output, @Nullable Ingredient catalyst, int recipeTime, int fuelCostPerTick, @Nullable ResourceLocation requireAdvancement) {
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
            MinecraftServer server = playerEntity.getServer();
            if (server == null) {
                return false;
            }
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
    public PlacementInfo placementInfo() {
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
                        ingredient = Optional.of(this.pattern.ingredients().get(this.pattern.width() - k - 1 + l * this.pattern.width()));
                    } else {
                        ingredient = Optional.of(this.pattern.ingredients().get(k + l * this.pattern.width()));
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
    public boolean matches(RecipeInput recipeInput, Level world) {
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
    public @NotNull ItemStack assemble(RecipeInput recipeInput, HolderLookup.Provider provider) {
        return this.output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= this.pattern.width() && height >= this.pattern.height();
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.Provider provider) {
        return this.output;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
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
                ResourceLocation.CODEC.optionalFieldOf("require_advancement").forGetter(r -> Optional.ofNullable(r.requireAdvancement))
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
            ResourceLocation requireAdvancement = null;
            if (buf.readBoolean()) {
                requireAdvancement = ResourceLocation.STREAM_CODEC.decode(buf);
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
                ResourceLocation.STREAM_CODEC.encode(buf, r.requireAdvancement);
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

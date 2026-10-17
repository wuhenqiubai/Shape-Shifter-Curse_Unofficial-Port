package net.onixary.shapeShifterCurseFabric.recipes.altar;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.recipes.RecipeSerializerRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

public class AltarShapelessRecipe extends AltarRecipe {
    public final ItemStack output;
    public final List<Ingredient> input;
    public final @Nullable Ingredient catalyst;
    public final int recipeTime;
    public final int fuelCostPerTick;
    private @Nullable PlacementInfo placementInfo;

    public final @Nullable Identifier requireAdvancement;

    public AltarShapelessRecipe(ItemStack output, List<Ingredient> input, @Nullable Ingredient catalyst, int recipeTime, int fuelCostPerTick, @Nullable Identifier requireAdvancement, int totalFuelCost) {
        this.output = output;
        this.input = input;
        this.recipeTime = recipeTime;
        this.catalyst = catalyst;
        this.fuelCostPerTick = fuelCostPerTick;
        this.requireAdvancement = requireAdvancement;
        this.totalFuelCost = totalFuelCost;
    }

    @Override
    public int recipeTime() {
        return recipeTime;
    }

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
            this.placementInfo = PlacementInfo.create(this.input);
        }
        return this.placementInfo;
    }

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
        return RecipeSerializerRegister.ALTAR_SHAPELESS_RECIPE;
    }

    public static class Serializer implements RecipeSerializer<AltarShapelessRecipe> {
        private static final MapCodec<AltarShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.output),
                Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(r -> r.input),
                Ingredient.CODEC.optionalFieldOf("catalyst").forGetter(r -> Optional.ofNullable(r.catalyst)),
                Codec.INT.optionalFieldOf("time", 200).forGetter(r -> r.recipeTime),
                Codec.INT.optionalFieldOf("fuel_cost", 1).forGetter(r -> r.fuelCostPerTick),
                Identifier.CODEC.optionalFieldOf("require_advancement").forGetter(r -> Optional.ofNullable(r.requireAdvancement)),
                Codec.INT.optionalFieldOf("moondust_cost")
                        .forGetter(r -> r.totalFuelCost >= 0 ? Optional.of(r.totalFuelCost / 800) : Optional.empty())
            ).apply(instance, (output, input, catalyst, time, fuelCost, requireAdvancement, moondustCost) ->
                new AltarShapelessRecipe(output, input, catalyst.orElse(null), time, fuelCost, requireAdvancement.orElse(null),
                        moondustCost.map(integer -> integer * 800).orElse(-1)))
        );

        private static final StreamCodec<RegistryFriendlyByteBuf, AltarShapelessRecipe> STREAM_CODEC = StreamCodec.of(
            Serializer::toNetwork, Serializer::fromNetwork
        );

        @Override
        public @NotNull MapCodec<AltarShapelessRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, AltarShapelessRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static AltarShapelessRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            Ingredient catalyst = null;
            if (buf.readBoolean()) {
                catalyst = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            }
            Identifier requireAdvancement = null;
            if (buf.readBoolean()) {
                requireAdvancement = Identifier.STREAM_CODEC.decode(buf);
            }
            List<Ingredient> list = Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
            ItemStack output = ItemStack.STREAM_CODEC.decode(buf);
            int time = buf.readVarInt();
            int fuelCost = buf.readVarInt();
            int totalFuelCost = buf.readVarInt();
            return new AltarShapelessRecipe(output, list, catalyst, time, fuelCost, requireAdvancement, totalFuelCost);
        }

        private static void toNetwork(RegistryFriendlyByteBuf packetByteBuf, AltarShapelessRecipe shapelessRecipe) {
            if (shapelessRecipe.catalyst != null) {
                packetByteBuf.writeBoolean(true);
                Ingredient.CONTENTS_STREAM_CODEC.encode(packetByteBuf, shapelessRecipe.catalyst);
            } else {
                packetByteBuf.writeBoolean(false);
            }
            if (shapelessRecipe.requireAdvancement != null) {
                packetByteBuf.writeBoolean(true);
                Identifier.STREAM_CODEC.encode(packetByteBuf, shapelessRecipe.requireAdvancement);
            } else {
                packetByteBuf.writeBoolean(false);
            }
            packetByteBuf.writeVarInt(shapelessRecipe.input.size());
            for (Ingredient ingredient : shapelessRecipe.input) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(packetByteBuf, ingredient);
            }
            ItemStack.STREAM_CODEC.encode(packetByteBuf, shapelessRecipe.output);
            packetByteBuf.writeVarInt(shapelessRecipe.recipeTime);
            packetByteBuf.writeVarInt(shapelessRecipe.fuelCostPerTick);
            packetByteBuf.writeVarInt(shapelessRecipe.totalFuelCost);
        }
    }
}

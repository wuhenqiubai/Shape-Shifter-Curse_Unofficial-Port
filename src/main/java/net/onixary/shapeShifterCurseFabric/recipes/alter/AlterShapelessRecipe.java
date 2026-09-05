package net.onixary.shapeShifterCurseFabric.recipes.alter;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.recipes.RecipeSerializerRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AlterShapelessRecipe extends AlterRecipe {
    public final ResourceLocation id;
    public final ItemStack output;
    public final NonNullList<Ingredient> input;
    public final @Nullable Ingredient catalyst;
    public final int recipeTime;
    public final int fuelCostPerTick;


    public AlterShapelessRecipe(ResourceLocation id, ItemStack output, NonNullList<Ingredient> input, Ingredient catalyst, int recipeTime, int fuelCostPerTick) {
        this.id = id;
        this.output = output;
        this.input = input;
        this.recipeTime = recipeTime;
        this.catalyst = catalyst;
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
    public int fuelUsage() {
        return fuelCostPerTick;
    }

    @Override
    public @NotNull ItemStack assemble(RecipeInput recipeInput, HolderLookup.Provider provider) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= this.input.size();
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.Provider provider) {
        return this.result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializerRegister.ALTER_SHAPELESS_RECIPE;
    }

    public static class Serializer implements RecipeSerializer<AlterShapelessRecipe> {
        public AlterShapelessRecipe read(ResourceLocation identifier, JsonObject jsonObject) {
            int time = GsonHelper.getAsInt(jsonObject, "time", 200);
            NonNullList<Ingredient> defaultedList = getIngredients(GsonHelper.getAsJsonArray(jsonObject, "ingredients"));
            Ingredient catalyst = null;
            if (jsonObject.has("catalyst")) {
                catalyst = Ingredient.fromJson(jsonObject.get("catalyst"), true);
            }
            int fuelCost = GsonHelper.getAsInt(jsonObject, "fuel_cost", 1);
            if (defaultedList.isEmpty()) {
                throw new JsonParseException("No ingredients for alter shapeless recipe");
            } else if (defaultedList.size() > 9) {
                throw new JsonParseException("Too many ingredients for alter shapeless recipe");
            } else {
                ItemStack itemStack = ShapedRecipe.outputFromJson(GsonHelper.getAsJsonObject(jsonObject, "result"));
                return new AlterShapelessRecipe(identifier, itemStack, defaultedList, catalyst, time, fuelCost);
            }
        }

        @Override
        public @NotNull MapCodec<AlterShapelessRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AlterShapelessRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        public AlterShapelessRecipe read(ResourceLocation identifier, FriendlyByteBuf packetByteBuf) {
            Ingredient catalyst = null;
            if (packetByteBuf.readBoolean()) {
                catalyst = Ingredient.fromPacket(packetByteBuf);
            }
            int i = packetByteBuf.readVarInt();
            NonNullList<Ingredient> defaultedList = NonNullList.withSize(i, Ingredient.EMPTY);
            for(int j = 0; j < defaultedList.size(); ++j) {
                defaultedList.set(j, Ingredient.fromPacket(packetByteBuf));
            }
            ItemStack itemStack = packetByteBuf.readItemStack();
            int time = packetByteBuf.readVarInt();
            int fuelCost = packetByteBuf.readVarInt();
            return new AlterShapelessRecipe(identifier, itemStack, defaultedList, catalyst, time, fuelCost);
        }

        public void write(FriendlyByteBuf packetByteBuf, AlterShapelessRecipe shapelessRecipe) {
            if (shapelessRecipe.catalyst != null) {
                packetByteBuf.writeBoolean(true);
                shapelessRecipe.catalyst.write(packetByteBuf);
            } else {
                packetByteBuf.writeBoolean(false);
            }
            packetByteBuf.writeVarInt(shapelessRecipe.input.size());
            for(Ingredient ingredient : shapelessRecipe.input) {
                ingredient.write(packetByteBuf);
            }
            packetByteBuf.writeItemStack(shapelessRecipe.output);
            packetByteBuf.writeVarInt(shapelessRecipe.recipeTime);
            packetByteBuf.writeVarInt(shapelessRecipe.fuelCostPerTick);
        }
    }
}

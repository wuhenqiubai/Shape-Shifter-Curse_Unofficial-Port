package net.onixary.shapeShifterCurseFabric.recipes.alter;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.recipes.RecipeSerializerRegister;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class AlterShapedRecipe extends AlterRecipe {
    public final String group;
    public final CraftingBookCategory category;
    public final ShapedRecipePattern pattern;
    public final ItemStack result;
    public final int recipeTime;

    public final int width;
    public final int height;

    public final NonNullList<Ingredient> input;
    public final @Nullable Ingredient catalyst;
    public final ItemStack output;
    public final ResourceLocation id;
    public final int fuelCostPerTick;

    public AlterShapedRecipe(ResourceLocation id, int width, int height, NonNullList<Ingredient> input, Ingredient catalyst, ItemStack output, int recipeTime, int fuelCostPerTick) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.input = input;
        this.output = output;
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
    public boolean matches(RecipeInput recipeInput, Level world) {
        if (this.catalyst != null) {
            ItemStack itemStack = recipeInput.getItem(9);
            if (!this.catalyst.test(itemStack)) {
                return false;
            }
        }

        for(int i = 0; i <= 3 - this.width; ++i) {
            for(int j = 0; j <= 3 - this.height; ++j) {
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
        public AlterShapedRecipe read(ResourceLocation identifier, JsonObject jsonObject) {
            int time = GsonHelper.getAsInt(jsonObject, "time", 200);
            Ingredient catalyst = null;
            if (jsonObject.has("catalyst")) {
                catalyst = Ingredient.fromJson(jsonObject.get("catalyst"), true);
            }
            int fuelCost = GsonHelper.getAsInt(jsonObject, "fuel_cost", 1);
            Map<String, Ingredient> map = readSymbols(GsonHelper.getAsJsonObject(jsonObject, "key"));
            String[] strings = removePadding(getPattern(GsonHelper.getAsJsonArray(jsonObject, "pattern")));
            int i = strings[0].length();
            int j = strings.length;
            NonNullList<Ingredient> defaultedList = createPatternMatrix(strings, map, i, j);
            ItemStack itemStack = ShapedRecipe.outputFromJson(GsonHelper.getObject(jsonObject, "result"));
            return new AlterShapedRecipe(identifier, i, j, defaultedList, catalyst, itemStack, time, fuelCost);
        }

        public AlterShapedRecipe read(ResourceLocation identifier, FriendlyByteBuf packetByteBuf) {
            Ingredient catalyst = null;
            if (packetByteBuf.readBoolean()) {
                catalyst = Ingredient.fromPacket(packetByteBuf);
            }
            int i = packetByteBuf.readVarInt();
            int j = packetByteBuf.readVarInt();
            NonNullList<Ingredient> defaultedList = NonNullList.withSize(i * j, Ingredient.EMPTY);
            for(int k = 0; k < defaultedList.size(); ++k) {
                defaultedList.set(k, Ingredient.fromPacket(packetByteBuf));
            }
            ItemStack itemStack = packetByteBuf.readItemStack();
            int time = packetByteBuf.readVarInt();
            int fuelCost = packetByteBuf.readVarInt();
            return new AlterShapedRecipe(identifier, i, j, defaultedList, catalyst, itemStack, time, fuelCost);
        }

        public void write(FriendlyByteBuf packetByteBuf, AlterShapedRecipe alterRecipe) {
            if (alterRecipe.catalyst != null) {
                packetByteBuf.writeBoolean(true);
                alterRecipe.catalyst.write(packetByteBuf);
            } else {
                packetByteBuf.writeBoolean(false);
            }
            packetByteBuf.writeVarInt(alterRecipe.width);
            packetByteBuf.writeVarInt(alterRecipe.height);
            for(Ingredient ingredient : alterRecipe.input) {
                ingredient.write(packetByteBuf);
            }
            packetByteBuf.writeItemStack(alterRecipe.output);
            packetByteBuf.writeVarInt(alterRecipe.recipeTime);
            packetByteBuf.writeVarInt(alterRecipe.fuelCostPerTick);
        }
    }
}

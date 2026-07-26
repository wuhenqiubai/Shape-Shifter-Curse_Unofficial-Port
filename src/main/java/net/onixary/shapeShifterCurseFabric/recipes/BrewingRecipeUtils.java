package net.onixary.shapeShifterCurseFabric.recipes;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// 酿造配方实际上不属于原版配方系统 但是都叫Recipe了 还是放到RecipePackage里吧
public class BrewingRecipeUtils {

	public static class DynamicRecipe<T> {
		public final T input;
		public final Ingredient ingredient;
		public final T output;
		public @Nullable Identifier targetForm;

		public DynamicRecipe(T input, Ingredient ingredient, T output, @Nullable Identifier targetForm) {
			this.input = input;
			this.ingredient = ingredient;
			this.output = output;
			this.targetForm = targetForm;
		}

		public boolean matchesInput(T otherInput) {
			if (input instanceof RegistryEntry<?> entry && otherInput instanceof RegistryEntry<?> otherEntry) {
				return entry.getKey().equals(otherEntry.getKey()) || entry.value() == otherEntry.value();
			}
			return input.equals(otherInput);
		}
	}

    private static final List<DynamicRecipe<Potion>> POTION_RECIPES = Lists.newArrayList();
    private static final List<DynamicRecipe<Item>> ITEM_RECIPES = Lists.newArrayList();

    public static void onLoadDynamicBrewingRecipesStart() {
        POTION_RECIPES.clear();
        ITEM_RECIPES.clear();
    }

	public static List<DynamicRecipe<Potion>> getPotionRecipes() {
		return POTION_RECIPES;
	}

	public static List<DynamicRecipe<Item>> getItemRecipes() {
		return ITEM_RECIPES;
    }

	public static void onLoadDynamicBrewingRecipesEnd() {
		// 动态配方存储在独立列表中，不再添加到原版配方注册表
		ShapeShifterCurseFabric.LOGGER.info("Loaded {} dynamic potion recipes and {} dynamic item recipes",
				POTION_RECIPES.size(), ITEM_RECIPES.size());
	}

    public static void registerPotionRecipe(JsonObject recipeJson) {
        if (recipeJson == null) {
            ShapeShifterCurseFabric.LOGGER.error("recipe json is null");
            return;
        }
        if (!recipeJson.has("type")) {
            ShapeShifterCurseFabric.LOGGER.error("recipe json has no type");
            return;
        }
        if (!recipeJson.has("input") || !recipeJson.has("ingredient") || !recipeJson.has("output")) {
            ShapeShifterCurseFabric.LOGGER.error("recipe json has no input or ingredient or output");
            return;
        }
        Identifier input = Identifier.tryParse(recipeJson.get("input").getAsString());
        Identifier ingredient = Identifier.tryParse(recipeJson.get("ingredient").getAsString());
        Identifier output = Identifier.tryParse(recipeJson.get("output").getAsString());
        Identifier targetForm = null;
        if (recipeJson.has("target_form")) {
            targetForm = Identifier.tryParse(recipeJson.get("target_form").getAsString());
        }
        if (input == null || ingredient == null || output == null) {
            ShapeShifterCurseFabric.LOGGER.error("recipe json has invalid input or ingredient or output");
            return;
        }
        Item ingredientItem = Registries.ITEM.get(ingredient);
        Ingredient ingredientObject = Ingredient.ofItems(new ItemConvertible[]{ingredientItem});
        switch (recipeJson.get("type").getAsString()) {
            case "potion" -> {
                Potion inputPotion = Registries.POTION.get(input);
                Potion outputPotion = Registries.POTION.get(output);
                POTION_RECIPES.add(new DynamicRecipe<>(inputPotion, ingredientObject, outputPotion, targetForm));
            }
            case "item" -> {
                Item inputItem = Registries.ITEM.get(input);
                Item outputItem = Registries.ITEM.get(output);
                ITEM_RECIPES.add(new DynamicRecipe<>(inputItem, ingredientObject, outputItem, targetForm));
            }
        }
        return;
    }
}

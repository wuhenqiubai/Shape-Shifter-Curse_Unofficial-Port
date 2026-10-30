package net.onixary.shapeShifterCurseFabric.recipes;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.recipes.altar.AltarRecipe;

public class RecipeUtils {
    public static final RecipeType<AltarRecipe> ALTAR_RECIPE = registerRecipeType(ShapeShifterCurseFabric.identifier("altar"));

    public static void register() {
        ALTAR_RECIPE.toString();
    };

    public static <T extends Recipe<?>> RecipeType<T> registerRecipeType(Identifier id) {
        return Registry.register(BuiltInRegistries.RECIPE_TYPE, id, new RecipeType<T>() {
            public String toString() {
                return id.toString();
            }
        });
    }
}

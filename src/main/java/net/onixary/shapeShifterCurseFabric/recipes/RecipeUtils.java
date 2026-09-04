package net.onixary.shapeShifterCurseFabric.recipes;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

public class RecipeUtils {
    public static final RecipeType<AlterRecipe> ALTER_RECIPE = registerRecipeType(ShapeShifterCurseFabric.identifier("alter"));

    public static void register() {
        // 用于加载静态注册
    };

    public static <T extends Recipe<?>> RecipeType<T> registerRecipeType(ResourceLocation id) {
        return Registry.register(BuiltInRegistries.RECIPE_TYPE, id, new RecipeType<T>() {
            public String toString() {
                return id.toString();
            }
        });
    }
}

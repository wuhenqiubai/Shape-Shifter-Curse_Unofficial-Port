package net.onixary.shapeShifterCurseFabric.recipes;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.recipes.altar.AltarRecipe;

public class RecipeUtils {
    public static final RecipeType<AltarRecipe> Altar_RECIPE = registerRecipeType(ShapeShifterCurseFabric.identifier("altar"));

    public static void register() {
        // 触发静态字段注册，否则 lazy 初始化会在 registry freeze 之后才注册 recipe_type（"Registry is already frozen"）
        Altar_RECIPE.toString();
    };

    public static <T extends Recipe<?>> RecipeType<T> registerRecipeType(Identifier id) {
        return Registry.register(BuiltInRegistries.RECIPE_TYPE, id, new RecipeType<T>() {
            public String toString() {
                return id.toString();
            }
        });
    }
}

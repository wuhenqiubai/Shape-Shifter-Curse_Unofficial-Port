package net.onixary.shapeShifterCurseFabric.recipes;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.recipes.altar.AltarShapedRecipe;
import net.onixary.shapeShifterCurseFabric.recipes.altar.AltarShapelessRecipe;
import net.onixary.shapeShifterCurseFabric.recipes.altar.BuiltinAltarRecipe;

public class RecipeSerializerRegister {
    public static RecipeSerializer<MorphScaleUpgradeRecipe> MORPH_SCALE_UPGRADE = register(ShapeShifterCurseFabric.identifier("morph_scale_upgrade"), new MorphScaleUpgradeRecipe.Serializer());
    public static RecipeSerializer<AltarShapedRecipe> Altar_SHAPED_RECIPE = register(ShapeShifterCurseFabric.identifier("altar_shaped"), new AltarShapedRecipe.Serializer());
    public static RecipeSerializer<AltarShapelessRecipe> Altar_SHAPELESS_RECIPE = register(ShapeShifterCurseFabric.identifier("altar_shapeless"), new AltarShapelessRecipe.Serializer());
    public static RecipeSerializer<BuiltinAltarRecipe> BUILTIN_Altar_RECIPE = register(ShapeShifterCurseFabric.identifier("builtin_altar"), new BuiltinAltarRecipe.Serializer());

    public static void register() {
        // 触发静态字段注册，否则 lazy 初始化会在 registry freeze 之后才注册 recipe_serializer
        MORPH_SCALE_UPGRADE.toString();
        Altar_SHAPED_RECIPE.toString();
        Altar_SHAPELESS_RECIPE.toString();
        BUILTIN_Altar_RECIPE.toString();
    }

    public static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(Identifier id, S serializer) {
        return (S)(Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id, serializer));
    };
}

package net.onixary.shapeShifterCurseFabric.recipes;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.recipes.alter.AlterShapedRecipe;
import net.onixary.shapeShifterCurseFabric.recipes.alter.AlterShapelessRecipe;

public class RecipeSerializerRegister {
    public static RecipeSerializer<MorphScaleUpgradeRecipe> MORPH_SCALE_UPGRADE = register(ShapeShifterCurseFabric.identifier("morph_scale_upgrade"), new MorphScaleUpgradeRecipe.Serializer());
    public static RecipeSerializer<AlterShapedRecipe> ALTER_SHAPED_RECIPE = register(ShapeShifterCurseFabric.identifier("alter_shaped"), new AlterShapedRecipe.Serializer());
    public static RecipeSerializer<AlterShapelessRecipe> ALTER_SHAPELESS_RECIPE = register(ShapeShifterCurseFabric.identifier("alter_shapeless"), new AlterShapelessRecipe.Serializer());

    public static void register() {
        // 用于加载静态注册
    }

    public static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(Identifier id, S serializer) {
        return (S)(Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id, serializer));
    };
}

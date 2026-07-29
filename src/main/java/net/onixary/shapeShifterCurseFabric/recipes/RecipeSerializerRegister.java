package net.onixary.shapeShifterCurseFabric.recipes;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.recipes.alter.AlterRecipe;

public class RecipeSerializerRegister {
    public static RecipeSerializer<MorphScaleUpgradeRecipe> MORPH_SCALE_UPGRADE = register(ShapeShifterCurseFabric.identifier("morph_scale_upgrade"), new MorphScaleUpgradeRecipe.Serializer());
    public static RecipeSerializer<AlterRecipe> ALTER_RECIPE = register(ShapeShifterCurseFabric.identifier("alter"), new AlterRecipe.Serializer());


    public static void register() {
        // 用于加载静态注册
    }


	public static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(Identifier id, S serializer) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id, serializer);
	}
}
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
    // 26.1: RecipeSerializer 由接口变成 record — record 是 final，原先
    // `class Serializer implements RecipeSerializer<X>` 的写法不再合法（"此处需要接口"）。
    // 现在改成直接构造 record：new RecipeSerializer<>(codec, streamCodec)，
    // 各配方的 Serializer 类退化为仅持有 public static 的 CODEC/STREAM_CODEC 的容器。
    // 字段名统一取全大写 ALTAR_*：全项目其余引用点（BuiltinAltarRecipe / AltarShapedRecipe /
    // AltarShapelessRecipe / AltarRecipeCheck）都用这个名字，26.1 侧此前的 Altar_SHAPED_RECIPE
    // 是大小写笔误，无任何外部引用，直接改齐。
    public static RecipeSerializer<MorphScaleUpgradeRecipe> MORPH_SCALE_UPGRADE = register(ShapeShifterCurseFabric.identifier("morph_scale_upgrade"),
            new RecipeSerializer<>(MorphScaleUpgradeRecipe.Serializer.CODEC, MorphScaleUpgradeRecipe.Serializer.PACKET_CODEC));
    public static RecipeSerializer<AltarShapedRecipe> ALTAR_SHAPED_RECIPE = register(ShapeShifterCurseFabric.identifier("altar_shaped"),
            new RecipeSerializer<>(AltarShapedRecipe.Serializer.CODEC, AltarShapedRecipe.Serializer.STREAM_CODEC));
    public static RecipeSerializer<AltarShapelessRecipe> ALTAR_SHAPELESS_RECIPE = register(ShapeShifterCurseFabric.identifier("altar_shapeless"),
            new RecipeSerializer<>(AltarShapelessRecipe.Serializer.CODEC, AltarShapelessRecipe.Serializer.STREAM_CODEC));
    public static RecipeSerializer<BuiltinAltarRecipe> BUILTIN_ALTAR_RECIPE = register(ShapeShifterCurseFabric.identifier("builtin_altar"),
            new RecipeSerializer<>(BuiltinAltarRecipe.Serializer.CODEC, BuiltinAltarRecipe.Serializer.STREAM_CODEC));

    public static void register() {
        // 触发静态字段注册，否则 lazy 初始化会在 registry freeze 之后才注册 recipe_serializer
        MORPH_SCALE_UPGRADE.toString();
        ALTAR_SHAPED_RECIPE.toString();
        ALTAR_SHAPELESS_RECIPE.toString();
        BUILTIN_ALTAR_RECIPE.toString();
    }

    public static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(Identifier id, S serializer) {
        return (S)(Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id, serializer));
    };
}

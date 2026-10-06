package net.onixary.shapeShifterCurseFabric.recipes.altar;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.onixary.shapeShifterCurseFabric.additional_power.IsMorphScaleItemCondition;
import net.onixary.shapeShifterCurseFabric.event.SSCEvent;

/**
 * 上游原文是 Yarn 写法，1.21.1 起「物品上的 NBT」已并入数据组件，需按组件读写：
 * <ul>
 *   <li>Yarn {@code ItemStack.isFood()} → Mojmap 由 {@link DataComponents#FOOD} 组件判定</li>
 *   <li>Yarn {@code ItemStack.getNbt()} → {@code getOrDefault(CUSTOM_DATA, CustomData.EMPTY).copyTag()}</li>
 *   <li>Yarn {@code ItemStack.getOrCreateNbt()} + 改写 → {@link CustomData#update}（内部 get-改-写回）</li>
 * </ul>
 */

public class RegBuiltinAlterRecipe {
    public static final ResourceLocation MorphScaleFood = ResourceLocation.fromNamespaceAndPath("shape_shifter_curse_fabric", "altar/morph_scale_food");
    public static final BuiltinAltarRecipe.BARecipeConfig MorphScaleFoodConfig = new BuiltinAltarRecipe.BARecipeConfigBuilder()
            .match(
                    (altarBlockEntity, world) -> {
                        int airCount = 0;
                        ItemStack foodStack = null;
                        for (int j = 0; j < 9; ++j) {
                            ItemStack itemStack = altarBlockEntity.getItem(j);
                            if (itemStack.isEmpty()) {
                                ++airCount;
                            } else {
                                foodStack = itemStack;
                            }
                        }
                        if (airCount != 8 || foodStack == null || foodStack.get(DataComponents.FOOD) == null) {
                            return false;
                        }
                        CompoundTag foodNBT = foodStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                        if (foodNBT.getBoolean(IsMorphScaleItemCondition.IsMorphScaleFoodTagName)) {
                            return false;
                        }
                        return true;
                    }
            )
            .craft(
                    (altarBlockEntity, registryManager) -> {
                        int airCount = 0;
                        ItemStack foodStack = null;
                        for (int j = 0; j < 9; ++j) {
                            ItemStack itemStack = altarBlockEntity.getItem(j);
                            if (itemStack.isEmpty()) {
                                ++airCount;
                            } else {
                                foodStack = itemStack;
                            }
                        }
                        if (airCount != 8 || foodStack == null || foodStack.get(DataComponents.FOOD) == null) {
                            return ItemStack.EMPTY;
                        }
                        ItemStack output = foodStack.copyWithCount(1);
                        CustomData.update(DataComponents.CUSTOM_DATA, output,
                                tag -> tag.putBoolean(IsMorphScaleItemCondition.IsMorphScaleFoodTagName, true));
                        return output;
                    }
            )
            .consumeInputs(
                    altarBlockEntity -> {
                        for (int j = 0; j < 9; ++j) {
                            ItemStack itemStack = altarBlockEntity.getItem(j);
                            if (!itemStack.isEmpty()) altarBlockEntity.removeItem(j, 1);
                        }
                    }
            )
            .fuelUsage(1)
            .recipeTime(200)
            .virtualOutput(
                    registryManager -> {
                        ItemStack example = new ItemStack(Items.APPLE);
                        CustomData.update(DataComponents.CUSTOM_DATA, example,
                                tag -> tag.putBoolean(IsMorphScaleItemCondition.IsMorphScaleFoodTagName, true));
                        return example;
                    }
            )
            .build();

    public static final BuiltinAltarRecipe MorphScaleFoodRecipe = new BuiltinAltarRecipe(MorphScaleFood, MorphScaleFoodConfig);

    static {
        MorphScaleFoodConfig.register(MorphScaleFood);
    }

    public static void init() {
        SSCEvent.BEFORE_APPLY_RECIPE.register(
                register -> {
                    register.accept(MorphScaleFood, MorphScaleFoodRecipe);
                }
        );
    }
}

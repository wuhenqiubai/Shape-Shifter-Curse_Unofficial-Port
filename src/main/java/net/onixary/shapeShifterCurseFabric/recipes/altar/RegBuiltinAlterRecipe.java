package net.onixary.shapeShifterCurseFabric.recipes.altar;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.onixary.shapeShifterCurseFabric.additional_power.IsMorphScaleItemCondition;
import net.onixary.shapeShifterCurseFabric.event.SSCEvent;

public class RegBuiltinAlterRecipe {
    public static final ResourceLocation MorphScaleFood = ResourceLocation.fromNamespaceAndPath("shape_shifter_curse_fabric", "altar/morph_scale_food");
    public static final ResourceLocation MorphScaleArmor = ResourceLocation.fromNamespaceAndPath("shape_shifter_curse_fabric", "altar/morph_scale_armor");

    public static final Item MorphScaleArmorCatalyst = Items.NETHER_STAR;

    public static final BuiltinAltarRecipe.BARecipeConfig MorphScaleFoodConfig = new BuiltinAltarRecipe.BARecipeConfigBuilder()
            .match(
                    (altarBlockEntity, world) -> {
                        ItemStack catalystItemStack = altarBlockEntity.getStack(9);
                        if (!catalystItemStack.isEmpty()) {
                            return false;
                        }
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

    public static final BuiltinAltarRecipe.BARecipeConfig MorphScaleArmorConfig = new BuiltinAltarRecipe.BARecipeConfigBuilder()
            .match(
                    (altarBlockEntity, world) -> {
                        ItemStack catalystItemStack = altarBlockEntity.getStack(9);
                        if (!catalystItemStack.isOf(MorphScaleArmorCatalyst)) {
                            return false;
                        }
                        int airCount = 0;
                        ItemStack unScaleStack = null;
                        for (int j = 0; j < 9; ++j) {
                            ItemStack itemStack = altarBlockEntity.getStack(j);
                            if (itemStack.isEmpty()) {
                                ++airCount;
                            } else {
                                unScaleStack = itemStack;
                            }
                        }
                        if (airCount != 8 || unScaleStack == null) {
                            return false;
                        }
                        NbtCompound ItemNBT = unScaleStack.getNbt();
                        if (ItemNBT != null && ItemNBT.getBoolean(IsMorphScaleItemCondition.IsMorphScaleArmorTagName)) {
                            return false;
                        }
                        return true;
                    }
            )
            .craft(
                    (altarBlockEntity, registryManager) -> {
                        int airCount = 0;
                        ItemStack unScaledStack = null;
                        for (int j = 0; j < 9; ++j) {
                            ItemStack itemStack = altarBlockEntity.getStack(j);
                            if (itemStack.isEmpty()) {
                                ++airCount;
                            } else {
                                unScaledStack = itemStack;
                            }
                        }
                        if (airCount != 8 || unScaledStack == null) {
                            return ItemStack.EMPTY;
                        }
                        ItemStack output = unScaledStack.copyWithCount(1);
                        NbtCompound nbtCompound = output.getOrCreateNbt();
                        nbtCompound.putBoolean(IsMorphScaleItemCondition.IsMorphScaleArmorTagName, true);
                        return output;
                    }
            )
            .consumeInputs(
                    altarBlockEntity -> {
                        for (int j = 0; j < 9; ++j) {
                            ItemStack itemStack = altarBlockEntity.getStack(j);
                            if (!itemStack.isEmpty()) altarBlockEntity.removeStack(j, 1);
                        }
                    }
            )
            .fuelUsage(64)
            .recipeTime(800)
            .virtualOutput(
                    registryManager -> {
                        ItemStack example = new ItemStack(Items.DIAMOND_CHESTPLATE);
                        NbtCompound nbtCompound = example.getOrCreateNbt();
                        nbtCompound.putBoolean(IsMorphScaleItemCondition.IsMorphScaleArmorTagName, true);
                        return example;
                    }
            )
            .catalyst(MorphScaleArmorCatalyst)
            .build();


    public static final BuiltinAltarRecipe MorphScaleArmorRecipe = new BuiltinAltarRecipe(MorphScaleArmor, MorphScaleArmorConfig);

    static {
        MorphScaleFoodConfig.register(MorphScaleFood);
        MorphScaleArmorConfig.register(MorphScaleArmor);
    }

    public static void init() {
        SSCEvent.BEFORE_APPLY_RECIPE.register(
                register -> {
                    register.accept(MorphScaleFood, MorphScaleFoodRecipe);
                    register.accept(MorphScaleArmor, MorphScaleArmorRecipe);
                }
        );
    }
}

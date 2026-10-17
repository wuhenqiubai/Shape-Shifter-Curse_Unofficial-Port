package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;
import net.onixary.shapeShifterCurseFabric.recipes.BrewingRecipeUtils;
import net.onixary.shapeShifterCurseFabric.status_effects.CTPUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionBrewing.class)
public class BrewingRecipeRegistryMixin {
	@Unique
    private static void setTargetForm(ItemStack stack, net.minecraft.resources.Identifier formID) {
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CTPUtils.setCTPFormIDToNBT(nbt, formID);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
    }

    @Inject(method = "isIngredient", at = @At("HEAD"), cancellable = true)
    private void ssc$isIngredient(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        for (BrewingRecipeUtils.DynamicRecipe<Potion> recipe : BrewingRecipeUtils.getPotionRecipes()) {
            if (recipe.ingredient.test(itemStack)) {
                cir.setReturnValue(true);
                return;
            }
        }
        for (BrewingRecipeUtils.DynamicRecipe<Item> recipe : BrewingRecipeUtils.getItemRecipes()) {
            if (recipe.ingredient.test(itemStack)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Inject(method = "mix", at = @At("HEAD"), cancellable = true)
    private void ssc$mix(ItemStack ingredient, ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
        if (input.isEmpty()) {
            return;
        }
        Item item = input.getItem();

        for (BrewingRecipeUtils.DynamicRecipe<Item> recipe : BrewingRecipeUtils.getItemRecipes()) {
            if (recipe.matchesInput(item) && recipe.ingredient.test(ingredient)) {
                ItemStack out;
                PotionContents contents = input.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                Holder<Potion> potionEntry = contents.potion().orElse(null);
                if (potionEntry != null) {
                    out = PotionContents.createItemStack(recipe.output, potionEntry);
                } else {
                    out = new ItemStack(recipe.output);
                }
                if (recipe.targetForm != null) {
                    setTargetForm(out, recipe.targetForm);
                }
                cir.setReturnValue(out);
                return;
            }
        }

        PotionContents potionContents = input.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        Holder<Potion> potionEntry = potionContents.potion().orElse(null);
        if (potionEntry == null) {
            return;
        }
        Potion potion = potionEntry.value();
        for (BrewingRecipeUtils.DynamicRecipe<Potion> recipe : BrewingRecipeUtils.getPotionRecipes()) {
            if (recipe.matchesInput(potion) && recipe.ingredient.test(ingredient)) {
                ItemStack out = PotionContents.createItemStack(input.getItem(), BuiltInRegistries.POTION.wrapAsHolder(recipe.output));
                if (recipe.targetForm != null) {
                    setTargetForm(out, recipe.targetForm);
                }
                cir.setReturnValue(out);
                return;
            }
        }
    }
}
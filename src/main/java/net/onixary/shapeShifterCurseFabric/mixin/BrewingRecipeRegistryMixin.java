package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.onixary.shapeShifterCurseFabric.recipes.BrewingRecipeUtils;
import net.onixary.shapeShifterCurseFabric.status_effects.CTPUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingRecipeRegistry.class)
public class BrewingRecipeRegistryMixin {
	@Unique
    private static void setTargetForm(ItemStack stack, net.minecraft.util.Identifier formID) {
        NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        CTPUtils.setCTPFormIDToNBT(nbt, formID);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    @Inject(method = "craft", at = @At("RETURN"))
    private static void craft(ItemStack ingredient, ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
        if (input.isEmpty()) {
            return;
        }
        ItemStack resultPotion = cir.getReturnValue();
        if (resultPotion.isEmpty()) {
            return;
        }

        // Check mod's dynamic item recipes
        Item item = input.getItem();
        for (BrewingRecipeUtils.DynamicRecipe<Item> recipe : BrewingRecipeUtils.getItemRecipes()) {
            if (recipe.matchesInput(item) && recipe.ingredient.test(ingredient)) {
                if (recipe.targetForm != null) {
                    setTargetForm(resultPotion, recipe.targetForm);
                }
                return;
            }
        }

        // Check mod's dynamic potion recipes
	    PotionContentsComponent potionContents = input.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
	    RegistryEntry<Potion> potionEntry = potionContents.potion().orElse(null);
	    if (potionEntry == null) {
            return;
        }
	    Potion potion = potionEntry.value();
        for (BrewingRecipeUtils.DynamicRecipe<Potion> recipe : BrewingRecipeUtils.getPotionRecipes()) {
            if (recipe.matchesInput(potion) && recipe.ingredient.test(ingredient)) {
                if (recipe.targetForm != null) {
                    setTargetForm(resultPotion, recipe.targetForm);
                }
                return;
            }
        }
    }
}

package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
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
    private static void setTargetForm(ItemStack stack, net.minecraft.resources.ResourceLocation formID) {
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CTPUtils.setCTPFormIDToNBT(nbt, formID);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
    }

    @Inject(method = "mix", at = @At("RETURN"))
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
	    PotionContents potionContents = input.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
	    Holder<Potion> potionEntry = potionContents.potion().orElse(null);
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

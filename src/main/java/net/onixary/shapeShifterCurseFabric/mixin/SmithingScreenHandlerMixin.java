package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.screen.SmithingScreenHandler;
import net.onixary.shapeShifterCurseFabric.recipes.ISmithingRecipeEX;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SmithingScreenHandler.class)
public class SmithingScreenHandlerMixin {
    @Inject(method = "onTakeOutput", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/CraftingResultInventory;unlockLastRecipe(Lnet/minecraft/entity/player/PlayerEntity;Ljava/util/List;)V", shift = At.Shift.AFTER), cancellable = true)
    public void onTakeOutput(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        SmithingScreenHandler realThis = (SmithingScreenHandler) (Object) this;
        SmithingRecipeInput recipeInput = new SmithingRecipeInput(realThis.input.getStack(0), realThis.input.getStack(1), realThis.input.getStack(2));
        List<RecipeEntry<SmithingRecipe>> list = realThis.world.getRecipeManager().getAllMatches(RecipeType.SMITHING, recipeInput, realThis.world);
        if (list.isEmpty()) {
            return;
        }
        SmithingRecipe recipe = list.getFirst().value();
        if (recipe instanceof ISmithingRecipeEX iSmithingRecipeEX) {
            iSmithingRecipeEX.onTakeOutput(realThis, player, stack);
            if (iSmithingRecipeEX.overrideVanillaOnTakeOutput()) {
                realThis.context.run((world, pos) -> world.syncWorldEvent(1044, pos, 0));
                ci.cancel();
            }
        }
    }
}

package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.onixary.shapeShifterCurseFabric.mixin.accessor.ItemCombinerMenuAccessor;
import net.onixary.shapeShifterCurseFabric.mixin.accessor.SmithingMenuAccessor;
import net.onixary.shapeShifterCurseFabric.recipes.ISmithingRecipeEX;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(SmithingMenu.class)
public class SmithingScreenHandlerMixin {
    @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
    public void onTake(Player player, ItemStack itemStack, CallbackInfo ci) {
        SmithingMenu realThis = (SmithingMenu) (Object) this;
        ItemCombinerMenuAccessor combiner = (ItemCombinerMenuAccessor) realThis;
        Level level = ((SmithingMenuAccessor) realThis).ssc_getLevel();
        Container inputSlots = combiner.ssc_getInputSlots();
        SmithingRecipeInput recipeInput = new SmithingRecipeInput(inputSlots.getItem(0), inputSlots.getItem(1), inputSlots.getItem(2));
        Optional<RecipeHolder<SmithingRecipe>> recipeOptional = level.getServer().getRecipeManager().getRecipeFor(RecipeType.SMITHING, recipeInput, level);
        if (recipeOptional.isEmpty()) {
            return;
        }
        SmithingRecipe recipe = recipeOptional.get().value();
        if (recipe instanceof ISmithingRecipeEX iSmithingRecipeEX && iSmithingRecipeEX.overrideVanillaOnTakeOutput()) {
            iSmithingRecipeEX.onTakeOutput(realThis, player, itemStack);
            itemStack.onCraftedBy(player, itemStack.getCount());
            combiner.ssc_getAccess().execute((world, pos) -> world.levelEvent(1044, pos, 0));
            ci.cancel();
        }
    }
}

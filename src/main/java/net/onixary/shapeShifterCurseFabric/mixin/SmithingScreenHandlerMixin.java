package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.onixary.shapeShifterCurseFabric.recipes.ISmithingRecipeEX;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SmithingMenu.class)
public class SmithingScreenHandlerMixin {
    @Inject(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ContainerLevelAccess;execute(Ljava/util/function/BiConsumer;)V"), cancellable = true)
    public void onTake(Player player, ItemStack itemStack, CallbackInfo ci) {
        SmithingMenu realThis = (SmithingMenu) (Object) this;
        SmithingRecipeInput recipeInput = new SmithingRecipeInput(realThis.inputSlots.getItem(0), realThis.inputSlots.getItem(1), realThis.inputSlots.getItem(2));
        List<RecipeHolder<SmithingRecipe>> list = realThis.level.getRecipeManager().getRecipesFor(RecipeType.SMITHING, recipeInput, realThis.level);
        if (list.isEmpty()) {
            return;
        }
        SmithingRecipe recipe = list.getFirst().value();
        if (recipe instanceof ISmithingRecipeEX iSmithingRecipeEX) {
            iSmithingRecipeEX.onTakeOutput(realThis, player, itemStack);
            if (iSmithingRecipeEX.overrideVanillaOnTakeOutput()) {
                realThis.access.execute((world, pos) -> world.levelEvent(1044, pos, 0));
                ci.cancel();
            }
        }
    }
}
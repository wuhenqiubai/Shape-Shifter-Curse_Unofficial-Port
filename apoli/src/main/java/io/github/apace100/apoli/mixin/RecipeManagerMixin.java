package io.github.apace100.apoli.mixin;

import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    //@Shadow protected abstract <C extends Container, T extends Recipe<C>> Map<Identifier, Recipe<C>> getAllOfType(RecipeType<T> type);

    //@Inject(method = "getFirstMatch", at = @At("HEAD"), cancellable = true)
    //private void prioritizeModifiedRecipes(RecipeType<Recipe<Container>> type, Container inventory, Level world, CallbackInfoReturnable<Optional<Recipe<Container>>> cir) {
        /* TODO: this
        Optional<Recipe<Inventory>> modifiedRecipe = this.getAllOfType(type).values().stream().flatMap((recipe) -> {
            return type.match(recipe, world, inventory).stream();
        }).filter(r -> r.getClass() == ModifiedCraftingRecipe.class).findFirst();
        if(modifiedRecipe.isPresent()) {
            cir.setReturnValue(modifiedRecipe);
        }*/
    //}
}

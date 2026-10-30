package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import net.onixary.shapeShifterCurseFabric.event.SSCEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    @Inject(
            method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Lnet/minecraft/world/item/crafting/RecipeMap;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/RecipeMap;create(Ljava/lang/Iterable;)Lnet/minecraft/world/item/crafting/RecipeMap;")
    )
    private void ssc$onPrepare(
            ResourceManager resourceManager, ProfilerFiller profilerFiller, CallbackInfoReturnable<RecipeMap> cir,
            @Local List<RecipeHolder<?>> recipeList
    ) {
        SSCEvent.BEFORE_APPLY_RECIPE.invoker().beforeApplyRecipe(
                (recipeID, recipe) -> ssc$registerRecipe(recipeList, recipeID, recipe)
        );
    }

    @Unique
    private static void ssc$registerRecipe(
            List<RecipeHolder<?>> recipeList,
            @Nullable Identifier recipeID, Recipe<?> recipe
    ) {
        if (recipeID == null) {
            return;
        }
        if (recipe.getType() == null) {
            return;
        }
        recipeList.add(new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, recipeID), recipe));
    }
}

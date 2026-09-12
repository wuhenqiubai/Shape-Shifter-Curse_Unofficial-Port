package net.onixary.shapeShifterCurseFabric.mixin;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.onixary.shapeShifterCurseFabric.event.SSCEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
    @Unique
    private void registerRecipe(
            ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> recipeBuilder,
            ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> recipeIDBuilder,
            @Nullable ResourceLocation recipeID, @NotNull Recipe<?> recipe)
    {
        // 1.21.1 Recipe 不再自带 id（id 由 RecipeHolder 管理），无法从 recipe 兜底取 id
        if (recipeID == null) {
            return;
        }
        if (recipe.getType() == null) {
            return;
        }
        RecipeHolder<?> recipeHolder = new RecipeHolder<>(recipeID, recipe);
        recipeBuilder.put(recipe.getType(), recipeHolder);
        recipeIDBuilder.put(recipeID, recipeHolder);
    }

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;", ordinal = 0))
    private void onApply(
            Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci,
            @Local(ordinal = 0) ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> recipeBuilder,
            @Local(ordinal = 0) ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> recipeIDBuilder
    ) {
        SSCEvent.BEFORE_APPLY_RECIPE.invoker().beforeApplyRecipe(
                (recipeID, recipe) -> registerRecipe(recipeBuilder, recipeIDBuilder, recipeID, recipe)
        );
    }
}

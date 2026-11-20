package net.onixary.shapeShifterCurseFabric.mixin;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.onixary.shapeShifterCurseFabric.event.SSCEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(RecipeMap.class)
public class RecipeMapMixin {
    @Unique
    private static Constructor<RecipeMap> ssc$recipeMapConstructor;

    @Unique
    private static RecipeMap ssc$createRecipeMap(Multimap<RecipeType<?>, RecipeHolder<?>> byType, Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> byKey) {
        try {
            if (ssc$recipeMapConstructor == null) {
                Constructor<RecipeMap> constructor = RecipeMap.class.getDeclaredConstructor(Multimap.class, Map.class);
                constructor.setAccessible(true);
                ssc$recipeMapConstructor = constructor;
            }
            return ssc$recipeMapConstructor.newInstance(byType, byKey);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to build RecipeMap with dynamic recipes", e);
        }
    }

    @ModifyReturnValue(method = "create", at = @At("RETURN"))
    private static RecipeMap ssc$addDynamicRecipes(RecipeMap original) {
        List<RecipeHolder<?>> extra = new ArrayList<>();
        SSCEvent.BEFORE_APPLY_RECIPE.invoker().beforeApplyRecipe((id, recipe) -> {
            if (id != null && recipe != null && recipe.getType() != null) {
                extra.add(new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, id), recipe));
            }
        });
        if (extra.isEmpty()) {
            return original;
        }
        ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> byType = ImmutableMultimap.builder();
        ImmutableMap.Builder<ResourceKey<Recipe<?>>, RecipeHolder<?>> byKey = ImmutableMap.builder();
        for (RecipeHolder<?> holder : original.values()) {
            byType.put(holder.value().getType(), holder);
            byKey.put(holder.id(), holder);
        }
        for (RecipeHolder<?> holder : extra) {
            byType.put(holder.value().getType(), holder);
            byKey.put(holder.id(), holder);
        }
        return ssc$createRecipeMap(byType.build(), byKey.build());
    }
}

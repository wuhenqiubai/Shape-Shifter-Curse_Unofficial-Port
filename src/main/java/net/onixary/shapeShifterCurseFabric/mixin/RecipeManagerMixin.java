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

/**
 * 用 Java 代码注册配方的扩展点（内置祭坛配方系统）。
 *
 * <p><b>1.21.11 注入点已变更：</b>1.21.1 时 {@code RecipeManager} 直接是 {@code SimpleJsonResourceReloadListener}，
 * 配方在 {@code apply} 里靠 {@code ImmutableMultimap.Builder}/{@code ImmutableMap.Builder} 组装，本 mixin 挂在
 * {@code apply} 的 {@code Map.entrySet()} 调用点上把自定义配方塞进那两个 builder。
 * 1.21.11 把 {@code RecipeManager} 改成了 {@code SimplePreparableReloadListener<RecipeMap>}：
 * 配方在 {@code prepare} 里扫成 {@code List<RecipeHolder<?>>}，再交给 {@code RecipeMap.create(...)}，
 * 而 {@code apply} 只剩一行 {@code this.recipes = recipeMap}——原来的 {@code entrySet()} 注入点在字节码里已不存在
 * （IDEA 报 "Could not resolve @At target"，编译器不报但运行时会崩）。
 * 因此改挂到 {@code prepare} 里 {@code RecipeMap.create} 调用之前，向那个 list 追加。</p>
 */
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
        // RecipeHolder 的首参是 ResourceKey<Recipe<?>>（1.21.11 起），不是 Identifier。
        // 注意：recipeID 需与 RecipeManager 内部 ResourceKey.create(Registries.RECIPE, id) 的约定一致。
        if (recipeID == null) {
            return;
        }
        if (recipe.getType() == null) {
            return;
        }
        recipeList.add(new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, recipeID), recipe));
    }
}

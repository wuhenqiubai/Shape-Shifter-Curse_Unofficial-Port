package net.onixary.shapeShifterCurseFabric.integration.origins.badge;

import io.github.apace100.calio.data.SerializableData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

public record CraftingRecipeBadge(Identifier spriteId,
                                  RecipeHolder<CraftingRecipe> recipe,
                                  @Nullable Component prefix,
                                  @Nullable Component suffix) implements Badge {

    public CraftingRecipeBadge(SerializableData.Instance instance) {
        this(instance.getId("sprite"),
            instance.get("recipe"),
            instance.get("prefix"),
            instance.get("suffix"));
    }

    @Override
    public boolean hasTooltip() {
        return true;
    }

    // 配方展示解析（getIngredientDisplays / getRecipeWidth / getRecipeHeight / getResultStack /
    // peekInputs）与 tooltip 构建都要用 Minecraft.getInstance()、Font 等客户端类，
    // 已整体移到纯客户端的 BadgeTooltipRenderers。

    @Override
    public SerializableData.Instance toData(SerializableData.Instance instance) {
        instance.set("sprite", spriteId);
        instance.set("recipe", recipe);
        instance.set("prefix", prefix);
        instance.set("suffix", suffix);
        return instance;
    }

    @Override
    public BadgeFactory getBadgeFactory() {
        return BadgeFactories.CRAFTING_RECIPE;
    }

}

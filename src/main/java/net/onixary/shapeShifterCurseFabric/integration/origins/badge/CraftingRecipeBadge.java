package net.onixary.shapeShifterCurseFabric.integration.origins.badge;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyCraftingPower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.calio.data.SerializableData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;
import net.onixary.shapeShifterCurseFabric.integration.origins.screen.tooltip.CraftingRecipeTooltipComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

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

    public NonNullList<ItemStack> peekInputs(float time) {
        int seed = Mth.floor(time / 30);
        NonNullList<ItemStack> inputs = NonNullList.withSize(9, ItemStack.EMPTY);
        List<Ingredient> ingredients = this.recipe.value().getIngredients();
        for(int index = 0; index < ingredients.size(); ++index) {
            ItemStack[] stacks = ingredients.get(index).getItems();
            if(stacks.length > 0) inputs.set(index, stacks[seed % stacks.length]);
        }
        return inputs;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public List<ClientTooltipComponent> getTooltipComponents(PowerType<?> powerType, int widthLimit, float time, Font textRenderer) {
        Minecraft client = Minecraft.getInstance();
        List<ClientTooltipComponent> tooltips = new LinkedList<>();
        if(Minecraft.getInstance().level == null) {
            Origins.LOGGER.warn("Could not construct crafting recipe badge, because world was null");
            return tooltips;
        }
        RegistryAccess registryManager = client.level.registryAccess();
        var outputStackReference = InventoryUtil.createStackReference(recipe.value().getResultItem(registryManager));
        PowerHolderComponent.getPowers(client.player, ModifyCraftingPower.class)
            .stream()
            .filter(p -> p.doesApply(recipe.id(), outputStackReference.get()))
            .max(Comparator.comparing(ModifyCraftingPower::getPriority))
            .ifPresent(p -> p.getNewResult(outputStackReference));

        int recipeWidth = recipe.value() instanceof ShapedRecipe shapedRecipe ? shapedRecipe.getWidth() : 3;

        if (client.options.advancedItemTooltips) {
            Component recipeIdText = Component.literal(recipe.id().toString()).withStyle(ChatFormatting.DARK_GRAY);
            widthLimit = Math.max(130, textRenderer.width(recipeIdText));
            if(prefix != null) TooltipBadge.addLines(tooltips, prefix, textRenderer, widthLimit);
            tooltips.add(new CraftingRecipeTooltipComponent(recipeWidth, this.peekInputs(time), outputStackReference.get()));
            if(suffix != null) TooltipBadge.addLines(tooltips, suffix, textRenderer, widthLimit);
            TooltipBadge.addLines(tooltips, recipeIdText, textRenderer, widthLimit);
        } else {
            widthLimit = 130;
            if(prefix != null) TooltipBadge.addLines(tooltips, prefix, textRenderer, widthLimit);
            tooltips.add(new CraftingRecipeTooltipComponent(recipeWidth, this.peekInputs(time), outputStackReference.get()));
            if(suffix != null) TooltipBadge.addLines(tooltips, suffix, textRenderer, widthLimit);
        }
        return tooltips;
    }

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
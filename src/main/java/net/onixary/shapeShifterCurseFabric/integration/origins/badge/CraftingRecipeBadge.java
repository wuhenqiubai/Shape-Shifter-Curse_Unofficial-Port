package net.onixary.shapeShifterCurseFabric.integration.origins.badge;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyCraftingPower;
import io.github.apace100.apoli.power.PowerType;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;
import net.onixary.shapeShifterCurseFabric.integration.origins.screen.tooltip.CraftingRecipeTooltipComponent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public record CraftingRecipeBadge(ResourceLocation spriteId,
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
        ItemStack output = recipe.value().getResultItem(registryManager);

        int recipeWidth = recipe.value() instanceof ShapedRecipe shapedRecipe ? shapedRecipe.getWidth() : 3;
        int recipeHeight = recipe.value() instanceof ShapedRecipe shapedRecipe ? shapedRecipe.getHeight() : 3;
        NonNullList<ItemStack> inputs = this.peekInputs(time);
        // Apoli-Legacy 2.11.4 移除了 alpha7 的 InventoryUtil.createStackReference / ModifyCraftingPower.getPriority，
        // doesApply/getNewResult 改为接收 CraftingInput，这里从合成材料构造。
        CraftingInput craftingInput = CraftingInput.of(recipeWidth, recipeHeight,
            new ArrayList<>(inputs.subList(0, Math.min(recipeWidth * recipeHeight, inputs.size()))));
        ItemStack[] outputRef = { output };
        PowerHolderComponent.getPowers(client.player, ModifyCraftingPower.class)
            .stream()
            .filter(p -> p.doesApply(craftingInput, recipe))
            .findFirst()
            .ifPresent(p -> outputRef[0] = p.getNewResult(craftingInput, recipe.value()));

        if (client.options.advancedItemTooltips) {
            Component recipeIdText = Component.literal(recipe.id().toString()).withStyle(ChatFormatting.DARK_GRAY);
            widthLimit = Math.max(130, textRenderer.width(recipeIdText));
            if(prefix != null) TooltipBadge.addLines(tooltips, prefix, textRenderer, widthLimit);
            tooltips.add(new CraftingRecipeTooltipComponent(recipeWidth, inputs, outputRef[0]));
            if(suffix != null) TooltipBadge.addLines(tooltips, suffix, textRenderer, widthLimit);
            TooltipBadge.addLines(tooltips, recipeIdText, textRenderer, widthLimit);
        } else {
            widthLimit = 130;
            if(prefix != null) TooltipBadge.addLines(tooltips, prefix, textRenderer, widthLimit);
            tooltips.add(new CraftingRecipeTooltipComponent(recipeWidth, inputs, outputRef[0]));
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

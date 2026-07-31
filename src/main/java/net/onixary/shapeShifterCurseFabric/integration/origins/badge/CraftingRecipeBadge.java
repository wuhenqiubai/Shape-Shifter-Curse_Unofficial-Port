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
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.*;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;
import net.onixary.shapeShifterCurseFabric.integration.origins.screen.tooltip.CraftingRecipeTooltipComponent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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

    private List<SlotDisplay> getIngredientDisplays() {
        for (RecipeDisplay display : this.recipe.value().display()) {
            if (display instanceof ShapedCraftingRecipeDisplay shaped) {
                return shaped.ingredients();
            } else if (display instanceof ShapelessCraftingRecipeDisplay shapeless) {
                return shapeless.ingredients();
            }
        }
        return List.of();
    }

    private int getRecipeWidth() {
        for (RecipeDisplay display : this.recipe.value().display()) {
            if (display instanceof ShapedCraftingRecipeDisplay shaped) {
                return shaped.width();
            }
        }
        return 3;
    }

    private int getRecipeHeight() {
        for (RecipeDisplay display : this.recipe.value().display()) {
            if (display instanceof ShapedCraftingRecipeDisplay shaped) {
                return shaped.height();
            }
        }
        return 3;
    }

    private ItemStack getResultStack() {
        for (RecipeDisplay display : this.recipe.value().display()) {
            SlotDisplay result;
            if (display instanceof ShapedCraftingRecipeDisplay shaped) {
                result = shaped.result();
            } else if (display instanceof ShapelessCraftingRecipeDisplay shapeless) {
                result = shapeless.result();
            } else {
                continue;
            }
            List<ItemStack> stacks = result.resolveForStacks(SlotDisplayContext.fromLevel(Minecraft.getInstance().level));
            if (!stacks.isEmpty()) return stacks.get(0);
        }
        return ItemStack.EMPTY;
    }

    public NonNullList<ItemStack> peekInputs(float time) {
        int seed = Mth.floor(time / 30);
        NonNullList<ItemStack> inputs = NonNullList.withSize(9, ItemStack.EMPTY);
        List<SlotDisplay> slotDisplays = getIngredientDisplays();
        if (slotDisplays.isEmpty()) return inputs;
        ContextMap contextMap = SlotDisplayContext.fromLevel(Minecraft.getInstance().level);
        for(int index = 0; index < slotDisplays.size() && index < 9; ++index) {
            List<ItemStack> stacks = slotDisplays.get(index).resolveForStacks(contextMap);
            if(!stacks.isEmpty()) inputs.set(index, stacks.get(seed % stacks.size()));
        }
        return inputs;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public List<ClientTooltipComponent> getTooltipComponents(PowerType<?> powerType, int widthLimit, float time, Font textRenderer) {
        Minecraft client = Minecraft.getInstance();
        List<ClientTooltipComponent> tooltips = new LinkedList<>();
        if(client.level == null) {
            Origins.LOGGER.warn("Could not construct crafting recipe badge, because world was null");
            return tooltips;
        }
        RegistryAccess registryManager = client.level.registryAccess();
        NonNullList<ItemStack> inputs = this.peekInputs(time);
        int recipeWidth = getRecipeWidth();
        int recipeHeight = getRecipeHeight();
        ItemStack output = getResultStack();

        // 应用 ModifyCraftingPower 修改输出
        ItemStack[] outputRef = { output };
        CraftingInput craftingInput = CraftingInput.of(recipeWidth, recipeHeight,
                new ArrayList<>(inputs.subList(0, Math.min(recipeWidth * recipeHeight, inputs.size()))));
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
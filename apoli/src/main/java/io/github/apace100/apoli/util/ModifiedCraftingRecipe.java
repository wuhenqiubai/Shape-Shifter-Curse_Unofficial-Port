package io.github.apace100.apoli.util;

import com.google.common.collect.Lists;
import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.mixin.CraftingInventoryAccessor;
import io.github.apace100.apoli.mixin.CraftingScreenHandlerAccessor;
import io.github.apace100.apoli.mixin.PlayerScreenHandlerAccessor;
import io.github.apace100.apoli.power.ModifyCraftingPower;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class ModifiedCraftingRecipe extends CustomRecipe {

    public static final RecipeSerializer<? extends CustomRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<CustomRecipe>(ModifiedCraftingRecipe::new);

    public ModifiedCraftingRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level world)
    {
        var inventory = ((CraftingInputContainerHolder) input).apoli$getCraftingContainer();

        if (inventory instanceof TransientCraftingContainer craftingInventory)
        {
            Optional<RecipeHolder<CraftingRecipe>> original = getOriginalMatch(input);
            if (original.isEmpty())
            {
                return false;
            }
            return getRecipes(craftingInventory).stream().anyMatch(r -> r.doesApply(input, original.get()));
        }

        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries)
    {
        var inventory = ((CraftingInputContainerHolder) input).apoli$getCraftingContainer();

        if (inventory instanceof TransientCraftingContainer craftingInventory)
        {
            Player player = getPlayerFromInventory(craftingInventory);
            if (player != null)
            {
                Optional<RecipeHolder<CraftingRecipe>> original = getOriginalMatch(input);
                if (original.isPresent())
                {
                    Optional<ModifyCraftingPower> optional = getRecipes(craftingInventory).stream().filter(r -> r.doesApply(input, original.get())).findFirst();
                    if (optional.isPresent())
                    {
                        ItemStack result = optional.get().getNewResult(input, original.get().value());
                        ((PowerCraftingInventory) craftingInventory).setPower(optional.get());
                        return result;
                    }
                }
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return SERIALIZER;
    }

    public static Player getPlayerFromInventory(TransientCraftingContainer inv) {
        AbstractContainerMenu handler = ((CraftingInventoryAccessor)inv).getMenu();
        return getPlayerFromHandler(handler);
    }

    public static Optional<BlockPos> getBlockFromInventory(TransientCraftingContainer inv) {
        AbstractContainerMenu handler = ((CraftingInventoryAccessor)inv).getMenu();
        if(handler instanceof CraftingMenu) {
            return ((CraftingScreenHandlerAccessor)handler).getAccess().evaluate((world, blockPos) -> blockPos);
        }
        return Optional.empty();
    }

    private List<ModifyCraftingPower> getRecipes(TransientCraftingContainer inv) {
        AbstractContainerMenu handler = ((CraftingInventoryAccessor)inv).getMenu();
        Player player = getPlayerFromHandler(handler);
        if(player != null) {
            return PowerHolderComponent.getPowers(player, ModifyCraftingPower.class);
        }
        return Lists.newArrayList();
    }

    private Optional<RecipeHolder<CraftingRecipe>> getOriginalMatch(CraftingInput input) {
        var inv = ((CraftingInputContainerHolder) input).apoli$getCraftingContainer();
        AbstractContainerMenu handler = ((CraftingInventoryAccessor)inv).getMenu();
        Player player = getPlayerFromHandler(handler);
        if(player != null && player.getServer() != null) {
            var recipeManager = player.getServer().getRecipeManager();

            for (RecipeHolder<?> recipe : recipeManager.getRecipes()) {
                var value = recipe.value();

                if (value instanceof ModifiedCraftingRecipe)
                    continue;

                if (value.getType() != RecipeType.CRAFTING)
                    continue;

                if (!(value instanceof CraftingRecipe craftingRecipe))
                    continue;

                if (craftingRecipe.matches(input, player.level())) {
                    return Optional.of((RecipeHolder<CraftingRecipe>) recipe);
                }
            }
        }
        return Optional.empty();
    }

    private static Player getPlayerFromHandler(AbstractContainerMenu screenHandler) {
        if(screenHandler instanceof CraftingMenu) {
            return ((CraftingScreenHandlerAccessor)screenHandler).getPlayer();
        }
        if(screenHandler instanceof InventoryMenu) {
            return ((PlayerScreenHandlerAccessor)screenHandler).getOwner();
        }
        return null;
    }
}

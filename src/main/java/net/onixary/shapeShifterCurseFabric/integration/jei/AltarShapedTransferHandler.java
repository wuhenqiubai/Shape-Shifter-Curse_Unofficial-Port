package net.onixary.shapeShifterCurseFabric.integration.jei;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.onixary.shapeShifterCurseFabric.custom_ui.AltarCraftUIHandler;
import net.onixary.shapeShifterCurseFabric.custom_ui.RegMenuType;
import net.onixary.shapeShifterCurseFabric.recipes.altar.AltarShapedRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AltarShapedTransferHandler implements IRecipeTransferHandler<AltarCraftUIHandler, AltarShapedRecipe> {

    @Override
    public @NotNull Class<? extends AltarCraftUIHandler> getContainerClass() {
        return AltarCraftUIHandler.class;
    }

    @Override
    public @NotNull Optional<MenuType<AltarCraftUIHandler>> getMenuType() {
        return Optional.of(RegMenuType.AltarCraftUI);
    }

    @Override
    public @NotNull RecipeType<AltarShapedRecipe> getRecipeType() {
        return SSC_JEI_Plugin.ALTAR_SHAPED;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(
            @NotNull AltarCraftUIHandler container,
            @NotNull AltarShapedRecipe recipe,
            @NotNull IRecipeSlotsView recipeSlots,
            @NotNull Player player,
            boolean maxTransfer,
            boolean doTransfer) {

        if (!doTransfer) {
            return null;
        }

        Minecraft client = Minecraft.getInstance();
        MultiPlayerGameMode im = client.gameMode;
        if (im == null) {
            return null;
        }

        int syncId = container.containerId;

        clearAltarSlots(container, im, player, syncId);

        List<Integer> targetSlots = new ArrayList<>();
        List<Ingredient> ingredients = new ArrayList<>();
        // targetSlots 的 stride 是祭坛 GUI 的 3 列；ingredients 的 stride 是 pattern 的紧凑宽度
        for (int row = 0; row < recipe.pattern.height(); row++) {
            for (int col = 0; col < recipe.pattern.width(); col++) {
                // 1.21.11: ingredients() 返回 List<Optional<Ingredient>>，空位即 empty（Ingredient.EMPTY 已移除）。
                // 空位不入列表，从而保持 targetSlots 与 ingredients 下标一一对应。
                Optional<Ingredient> ing = recipe.pattern.ingredients().get(col + row * recipe.pattern.width());
                if (ing.isEmpty()) {
                    continue;
                }
                targetSlots.add(col + row * 3);
                ingredients.add(ing.get());
            }
        }

        int count = maxTransfer ? calcMaxCount(container, ingredients) : 1;

        for (int i = 0; i < ingredients.size(); i++) {
            Ingredient ing = ingredients.get(i);
            if (!ing.isEmpty()) {
                moveNTo(container, im, player, syncId, ing, targetSlots.get(i), count);
            }
        }
        if (recipe.catalyst != null) {
            moveNTo(container, im, player, syncId, recipe.catalyst, 9, 1);
        }

        return null;
    }

    private void clearAltarSlots(AltarCraftUIHandler container, MultiPlayerGameMode im, Player player, int syncId) {
        for (int i = 0; i <= 9; i++) {
            Slot slot = container.getSlot(i);
            if (slot.hasItem()) {
                im.handleContainerInput(syncId, i, 0, ContainerInput.PICKUP, player);
                int emptyPlayerSlot = findEmptyPlayerSlot(container);
                if (emptyPlayerSlot != -1) {
                    im.handleContainerInput(syncId, emptyPlayerSlot, 0, ContainerInput.PICKUP, player);
                } else {
                    im.handleContainerInput(syncId, -999, 0, ContainerInput.PICKUP, player);
                }
            }
        }
    }

    private int findEmptyPlayerSlot(AltarCraftUIHandler container) {
        for (int i = 12; i < 48; i++) {
            if (!container.getSlot(i).hasItem()) {
                return i;
            }
        }
        return -1;
    }

    private int calcMaxCount(AltarCraftUIHandler container, List<Ingredient> ingredients) {
        int min = 64;
        for (Ingredient ing : ingredients) {
            if (ing.isEmpty()) continue;
            min = Math.min(min, countInPlayer(container, ing));
        }
        return min;
    }

    private int countInPlayer(AltarCraftUIHandler container, Ingredient ing) {
        int count = 0;
        for (int i = 12; i < 48; i++) {
            ItemStack stack = container.getSlot(i).getItem();
            if (!stack.isEmpty() && ing.test(stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private void moveNTo(AltarCraftUIHandler container, MultiPlayerGameMode im,
                         Player player, int syncId, Ingredient ing, int targetSlot, int n) {
        if (n <= 0) {
            return;
        }
        int remaining = n;
        for (int i = 12; i < 48 && remaining > 0; i++) {
            Slot slot = container.getSlot(i);
            ItemStack stack = slot.getItem();
            if (stack.isEmpty() || !ing.test(stack)) {
                continue;
            }
            int available = stack.getCount();
            int take = Math.min(remaining, available);

            im.handleContainerInput(syncId, i, 0, ContainerInput.PICKUP, player);

            if (take == available) {
                im.handleContainerInput(syncId, targetSlot, 0, ContainerInput.PICKUP, player);
            } else {
                for (int j = 0; j < take; j++) {
                    im.handleContainerInput(syncId, targetSlot, 1, ContainerInput.PICKUP, player);
                }
                im.handleContainerInput(syncId, i, 0, ContainerInput.PICKUP, player);
            }

            remaining -= take;
        }
    }
}
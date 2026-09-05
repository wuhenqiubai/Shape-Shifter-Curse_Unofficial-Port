package net.onixary.shapeShifterCurseFabric.custom_ui;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.onixary.shapeShifterCurseFabric.blocks.block_entity.AlterBlockEntity;
import net.onixary.shapeShifterCurseFabric.custom_ui.ui_part.AlterOutputSlot;
import net.onixary.shapeShifterCurseFabric.recipes.alter.AlterRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AlterCraftUIHandler extends RecipeBookMenu {
    public final Inventory playerInventory;
    public final Container alterBlockEntity;
    public final ContainerLevelAccess context;
    public final Player player;
    public final Level world;
    public final ContainerData propertyDelegate;

    public static AlterCraftUIHandler createMenu(int i, Inventory inventory) {
        return new AlterCraftUIHandler(RegMenuType.AlterCraftUI, i, inventory, new SimpleContainer(11), ContainerLevelAccess.NULL, new SimpleContainerData(4));
    }

    public AlterCraftUIHandler(MenuType<?> screenHandlerType, int syncId, Inventory playerInventory, Container alterBlockEntity, ContainerLevelAccess context, ContainerData propertyDelegate) {
        super(screenHandlerType, syncId);
        this.playerInventory = playerInventory;
        this.alterBlockEntity = alterBlockEntity;
        this.context = context;
        this.player = playerInventory.player;
        this.world = playerInventory.player.level();
        this.propertyDelegate = propertyDelegate;

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 3; ++j) {
                this.addSlot(new Slot(this.alterBlockEntity, j + i * 3, 30 + j * 18, 17 + i * 18));
            }
        }

        this.addSlot(new Slot(this.alterBlockEntity, 9, 152, 57));
        this.addSlot(new AlterOutputSlot(this.alterBlockEntity, 10, 124, 35));

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for(int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }

        this.addDataSlots(propertyDelegate);
    }

    @Override
    public void fillCraftSlotsStackedContents(StackedItemContents finder) {
        if (this.alterBlockEntity instanceof AlterBlockEntity realAlter) {
            realAlter.fillStackedContents(finder);
        }
    }

    public int getGridWidth() {
        return 3;
    }

    public int getGridHeight() {
        return 3;
    }

    @Override
    public RecipeBookMenu.PostPlaceAction handlePlacement(boolean bl, boolean bl2, RecipeHolder<?> recipeHolder, ServerLevel serverLevel, Inventory inventory) {
        RecipeHolder<AlterRecipe> recipeHolder2 = (RecipeHolder<AlterRecipe>) recipeHolder;
        List<Slot> inputGrid = this.slots.subList(0, 9);
        return ServerPlaceRecipe.placeRecipe(new ServerPlaceRecipe.CraftingMenuAccess<AlterRecipe>() {
            @Override
            public void fillCraftSlotsStackedContents(StackedItemContents contents) {
                AlterCraftUIHandler.this.fillCraftSlotsStackedContents(contents);
            }
            @Override
            public void clearCraftingContent() {
                for (int i = 0; i < AlterCraftUIHandler.this.alterBlockEntity.getContainerSize(); ++i) {
                    if (i == 9) continue;
                    AlterCraftUIHandler.this.getSlot(i).set(ItemStack.EMPTY);
                }
            }
            @Override
            public boolean recipeMatches(RecipeHolder<AlterRecipe> r) {
                if (AlterCraftUIHandler.this.alterBlockEntity instanceof AlterBlockEntity realAlter) {
                    return r.value().matches(realAlter.craftInput(), AlterCraftUIHandler.this.world);
                }
                return false;
            }
        }, getGridWidth(), getGridHeight(), inputGrid, inputGrid, inventory, recipeHolder2, bl, bl2);
    }

    @Override
    public @NotNull RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int slotIndex) {
        // 0~8 -> Input
        // 9 -> Fuel
        // 10 -> Output
        // 11~37 -> Player Inventory
        // 38~46 -> Player Hotbar
        Slot slot = this.slots.get(slotIndex);
        ItemStack slotItem = slot.hasItem() ? slot.getItem() : ItemStack.EMPTY;
        ItemStack slotItemCopy = slotItem.copy();
        if (slotIndex >= 0 && slotIndex < 11) {
            if (!this.moveItemStackTo(slotItem, 11, 47, slotIndex == 10)) {
                return ItemStack.EMPTY;
            }
            if (slotIndex == 0) {
                slot.onQuickCraft(slotItem, slotItemCopy);
            }
        }
        else if (slotIndex >= 11 && slotIndex < 47) {
            if (AlterBlockEntity.canFuel(slotItem)) {
                if (!this.moveItemStackTo(slotItem, 9, 10, false)) {
                    if (!this.moveItemStackTo(slotItem, 0, 9, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
            if (!this.moveItemStackTo(slotItem, 0, 9, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (slotItem.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (slotItem.getCount() == slotItemCopy.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, slotItem);

        return ItemStack.EMPTY;
    }

    public int getNowProgress() {
        return this.propertyDelegate.get(0);
    }

    public int getMaxProgress() {
        return this.propertyDelegate.get(1);
    }

    public int getNowFuel() {
        return this.propertyDelegate.get(2);
    }

    public int getMaxFuel() {
        return this.propertyDelegate.get(3);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.context, player, Blocks.CRAFTING_TABLE);
    }
}

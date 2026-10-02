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
import net.onixary.shapeShifterCurseFabric.blocks.block_entity.AltarBlockEntity;
import net.onixary.shapeShifterCurseFabric.custom_ui.ui_part.AltarOutputSlot;
import net.onixary.shapeShifterCurseFabric.recipes.altar.AltarRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AltarCraftUIHandler extends RecipeBookMenu {
    public final Inventory playerInventory;
    public final Container altarBlockEntity;
    public final ContainerLevelAccess context;
    public final Player player;
    public final Level world;
    public final ContainerData propertyDelegate;

    public static AltarCraftUIHandler createMenu(int i, Inventory inventory) {
        // ⚠ 槽数必须与 AltarBlockEntity.propertyDelegate 的 getCount() 一致（现为 4）：
        //   slot2=fuelTime 低16位、slot3=高16位。此处若只给 3，服务端广播的 slot3 会在客户端越界
        //   （ClientboundContainerSetDataPacket 处理失败），且 getNowFuel() 读 slot3 时直接崩渲染。
        return new AltarCraftUIHandler(RegMenuType.AltarCraftUI, i, inventory, new SimpleContainer(12), ContainerLevelAccess.NULL, new SimpleContainerData(4));
    }

    public AltarCraftUIHandler(MenuType<?> screenHandlerType, int syncId, Inventory playerInventory, Container altarBlockEntity, ContainerLevelAccess context, ContainerData propertyDelegate) {
        super(screenHandlerType, syncId);
        this.playerInventory = playerInventory;
        this.altarBlockEntity = altarBlockEntity;
        this.context = context;
        this.player = playerInventory.player;
        this.world = playerInventory.player.level();
        this.propertyDelegate = propertyDelegate;

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 3; ++j) {
                this.addSlot(new Slot(this.altarBlockEntity, j + i * 3, 26 + j * 18, 17 + i * 18));
            }
        }

        this.addSlot(new Slot(this.altarBlockEntity, 9, 97, 22));
        this.addSlot(new Slot(this.altarBlockEntity, 10, 84, 53));
        this.addSlot(new AltarOutputSlot(this.altarBlockEntity, 11, 134, 35));

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 7 + j * 18, 83 + i * 18));
            }
        }

        for(int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 7 + i * 18, 141));
        }

        this.addDataSlots(propertyDelegate);
    }

    @Override
    public void fillCraftSlotsStackedContents(StackedItemContents finder) {
        if (this.altarBlockEntity instanceof AltarBlockEntity realAltar) {
            realAltar.fillStackedContents(finder);
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
        RecipeHolder<AltarRecipe> recipeHolder2 = (RecipeHolder<AltarRecipe>) recipeHolder;
        List<Slot> inputGrid = this.slots.subList(0, 9);
        return ServerPlaceRecipe.placeRecipe(new ServerPlaceRecipe.CraftingMenuAccess<AltarRecipe>() {
            @Override
            public void fillCraftSlotsStackedContents(StackedItemContents contents) {
                AltarCraftUIHandler.this.fillCraftSlotsStackedContents(contents);
            }
            @Override
            public void clearCraftingContent() {
                for (int i = 0; i < AltarCraftUIHandler.this.altarBlockEntity.getContainerSize(); ++i) {
                    if (i == 9) continue;
                    AltarCraftUIHandler.this.getSlot(i).set(ItemStack.EMPTY);
                }
            }
            @Override
            public boolean recipeMatches(RecipeHolder<AltarRecipe> r) {
                if (AltarCraftUIHandler.this.altarBlockEntity instanceof AltarBlockEntity realAltar) {
                    return r.value().matches(realAltar.craftInput(), AltarCraftUIHandler.this.world);
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
        // 9 -> Catalyst
        // 10 -> Fuel
        // 11 -> Output
        // 12~38 -> Player Inventory
        // 39~47 -> Player Hotbar
        Slot slot = this.slots.get(slotIndex);
        ItemStack slotItem = slot.hasItem() ? slot.getItem() : ItemStack.EMPTY;
        ItemStack slotItemCopy = slotItem.copy();
        if (slotIndex >= 0 && slotIndex < 12) {
            if (!this.moveItemStackTo(slotItem, 12, 47, slotIndex == 10)) {
                return ItemStack.EMPTY;
            }
            if (slotIndex == 0) {
                slot.onQuickCraft(slotItem, slotItemCopy);
            }
        }
        else if (slotIndex >= 12 && slotIndex < 48) {
            if (AltarBlockEntity.canFuel(slotItem)) {
                if (!this.moveItemStackTo(slotItem, 10, 11, false)) {
                    if (!this.moveItemStackTo(slotItem, 0, 10, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
            if (!this.moveItemStackTo(slotItem, 0, 10, false)) {
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
        // data slot 以 16-bit(short) 传输，原先只传 slot2=fuelTime 会被 writeShort 截断成负值。
        // 现在 slot2=低16位、slot3=高16位，这里拼回完整 fuelTime（无损）。
        return (this.propertyDelegate.get(2) & 0xFFFF) | ((this.propertyDelegate.get(3) & 0xFFFF) << 16);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.context, player, Blocks.CRAFTING_TABLE);
    }
}

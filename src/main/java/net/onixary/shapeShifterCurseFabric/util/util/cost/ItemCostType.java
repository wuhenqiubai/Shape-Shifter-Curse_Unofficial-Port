package net.onixary.shapeShifterCurseFabric.util.util.cost;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.util.ClientUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemCostType implements IFUSDrawableCostType<ItemCostType> {
    private static final Identifier id = ShapeShifterCurseFabric.identifier("item");
    public static final Identifier TEXTURE = ShapeShifterCurseFabric.identifier("textures/gui/shape_shifter_tuner_ui.png");
    public static final int TEXTURE_WIDTH = 454;
    public static final int TEXTURE_HEIGHT = 190;


    @Override
    public void drawIcon(GuiGraphics context, @NotNull ICost costObject, @Nullable Player player, int x, int y, int z) {
        CostTypeIcons.drawIcon(this, context, costObject, player, x, y, z);
    }

    @Override
    public Identifier getID() {
        return id;
    }

    @Override
    public Component getAmountText(@NotNull ICost costObject, @Nullable Player player) {
        if (!(costObject instanceof ItemCost cost) || player == null) {
            return IFUSDrawableCostType.super.getAmountText(costObject, player);
        }
        return Component.literal(String.valueOf(cost.getAmount()))
                .append(Component.translatable("gui.shape-shifter-curse.perk.cost_separator"))
                .append(String.valueOf(getAvailableAmount(cost, player)));
    }

    private int getAvailableAmount(ItemCost cost, Player player) {
        Inventory inventory = player.getInventory();
        int total = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(cost.getExampleStack(), stack)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    @Override
    public boolean canPay(@NotNull ICost costObject, @Nullable Player player) {
        if (!(costObject instanceof ItemCost cost)) {
            throw new RuntimeException("ItemCostType.canPay costObject must be ItemCost");
        }
        ItemStack exampleStack = cost.getExampleStack();
        int amount = cost.getAmount();
        if (exampleStack.isEmpty() || amount <= 0) {
            return true;
        }
        if (player == null) {
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                player = ClientUtils.getPlayer();
            } else {
                throw new RuntimeException("CostType.canPay Player Argument In ServerSide Must NotNull");
            }
        }
        return getAvailableAmount(cost, player) >= amount;
    }

    @Override
    public void pay(@NotNull ICost costObject, @NotNull Player player) {
        if (!(costObject instanceof ItemCost cost)) {
            throw new RuntimeException("ItemCostType.pay costObject must be ItemCost");
        }
        ItemStack exampleStack = cost.getExampleStack();
        int amount = cost.getAmount();
        if (exampleStack.isEmpty() || amount <= 0) {
            return;
        }
        Inventory playerInventory = player.getInventory();
        int remaining = amount;
        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            if (remaining <= 0) {
                break;
            }
            ItemStack stack = playerInventory.getItem(i);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(exampleStack, stack)) {
                int take = Math.min(stack.getCount(), remaining);
                stack.shrink(take);
                remaining -= take;
            }
        }
        if (remaining > 0) {
            ShapeShifterCurseFabric.LOGGER.warn("ItemCostType.pay: insufficient items, remaining {}", remaining);
        }
    }

    @Override
    public Component getResourceNotEnoughText(@NotNull ICost costObject, @Nullable Player player) {
        return Component.translatable("gui.shape-shifter-curse.perk.insufficient_items");
    }
}

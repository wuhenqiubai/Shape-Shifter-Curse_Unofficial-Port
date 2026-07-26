package net.onixary.shapeShifterCurseFabric.recipes;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;

public interface ISmithingRecipeEX {
    public boolean overrideVanillaOnTakeOutput();

    public void onTakeOutput(SmithingMenu screenHandler, Player player, ItemStack stack);
}

package net.onixary.shapeShifterCurseFabric.custom_ui.ui_part;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AlterOutputSlot extends Slot {
    public AlterOutputSlot(Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    public boolean mayPlace(ItemStack stack) {
        return false;
    }
}

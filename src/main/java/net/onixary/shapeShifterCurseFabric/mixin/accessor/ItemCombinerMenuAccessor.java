package net.onixary.shapeShifterCurseFabric.mixin.accessor;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemCombinerMenu.class)
public interface ItemCombinerMenuAccessor {
    @Accessor("inputSlots")
    Container ssc_getInputSlots();

    @Accessor("access")
    ContainerLevelAccess ssc_getAccess();
}

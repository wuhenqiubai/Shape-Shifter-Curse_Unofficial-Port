package net.onixary.shapeShifterCurseFabric.mixin.accessor;

import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SmithingMenu.class)
public interface SmithingMenuAccessor {
    @Accessor("level")
    Level ssc_getLevel();

    @Invoker("shrinkStackInSlot")
    void ssc_shrinkStackInSlot(int index);
}

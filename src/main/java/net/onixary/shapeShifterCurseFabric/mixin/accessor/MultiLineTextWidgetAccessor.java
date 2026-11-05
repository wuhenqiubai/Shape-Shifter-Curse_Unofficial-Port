package net.onixary.shapeShifterCurseFabric.mixin.accessor;

import net.minecraft.client.gui.components.MultiLineTextWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultiLineTextWidget.class)
public interface MultiLineTextWidgetAccessor {
    @Accessor("centered")
    boolean ssc_isCentered();
}

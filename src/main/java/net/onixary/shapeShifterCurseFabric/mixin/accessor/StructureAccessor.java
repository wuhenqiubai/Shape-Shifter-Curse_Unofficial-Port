package net.onixary.shapeShifterCurseFabric.mixin.accessor;

import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Structure.class)
public interface StructureAccessor {
    @Accessor("settings")
    @Mutable
    void ssc_setSettings(Structure.StructureSettings settings);
}

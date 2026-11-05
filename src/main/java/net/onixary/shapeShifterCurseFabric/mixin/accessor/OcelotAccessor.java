package net.onixary.shapeShifterCurseFabric.mixin.accessor;

import net.minecraft.world.entity.animal.feline.Ocelot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Ocelot.class)
public interface OcelotAccessor {
    @Invoker("isTrusting")
    boolean ssc_isTrusting();
}

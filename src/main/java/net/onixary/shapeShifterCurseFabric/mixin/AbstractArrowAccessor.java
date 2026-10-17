package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractArrow.class)
public interface AbstractArrowAccessor {
    @Accessor("baseDamage")
    double ssc$getBaseDamage();

    @Accessor("baseDamage")
    void ssc$setBaseDamage(double value);
}

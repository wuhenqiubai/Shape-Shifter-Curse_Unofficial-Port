package net.onixary.shapeShifterCurseFabric.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.power.MultiplePowerType;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Prevents NPE when {@link MultiplePowerType#getSubPowers()} is called
 * before {@link MultiplePowerType#setSubPowers} has been set.
 * This can happen during data pack loading when GlobalPowerSet processing
 * encounters a MultiplePowerType whose sub-powers haven't been loaded yet.
 */
@Mixin(value = MultiplePowerType.class, remap = false)
public abstract class MultiplePowerTypeMixin {

    @ModifyReturnValue(method = "getSubPowers", remap = false, at = @At("RETURN"))
    private ImmutableList<Identifier> ssc$preventNullSubPowers(ImmutableList<Identifier> original) {
        return original != null ? original : ImmutableList.of();
    }
}
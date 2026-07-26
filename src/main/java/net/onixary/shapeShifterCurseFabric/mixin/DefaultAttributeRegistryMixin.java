package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.onixary.shapeShifterCurseFabric.util.EntityAttributeRegister;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(DefaultAttributes.class)
public class DefaultAttributeRegistryMixin {
    @Inject(method = "getSupplier", at = @At("HEAD"), cancellable = true)
    private static void get(EntityType<? extends LivingEntity> type, CallbackInfoReturnable<AttributeSupplier> cir) {
        if (EntityAttributeRegister.ShouldUseThisSystem()) {
            Optional<AttributeSupplier> optional = EntityAttributeRegister.getAttributes(type);
            optional.ifPresent(cir::setReturnValue);
        }
    }

}

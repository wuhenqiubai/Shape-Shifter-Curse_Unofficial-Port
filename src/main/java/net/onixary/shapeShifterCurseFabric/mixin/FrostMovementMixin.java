package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.additional_power.FrostDivePower;
import net.onixary.shapeShifterCurseFabric.status_effects.RegOtherStatusEffects;
import net.onixary.shapeShifterCurseFabric.status_effects.other_effects.FrostClawEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public class FrostMovementMixin {
    @ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true)
    private Vec3 frostMovement(Vec3 delta) {
        Entity self = (Entity) (Object) this;
        delta = FrostDivePower.constrainMovement(self, delta);
        if (self instanceof LivingEntity living && living.hasEffect(
                net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(RegOtherStatusEffects.FROST_CLAW))) {
            FrostClawEffect.beforeMove(living, delta);
        }
        return delta;
    }
}

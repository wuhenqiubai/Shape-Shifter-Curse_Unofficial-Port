package net.onixary.shapeShifterCurseFabric.integration.origins.mixin;

import io.github.apace100.calio.data.SerializableData;
import net.minecraft.world.entity.Entity;
import net.onixary.shapeShifterCurseFabric.integration.origins.power.ModifyTypeTagPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Sets up the entity context ThreadLocal before {@link InTagCondition}
 * checks entity type tag membership, so that {@link ModifyTypeTagMixin}
 * can check {@link ModifyTypeTagPower} in addition to vanilla tags.
 */
@Mixin(InTagCondition.class)
public class InTagConditionMixin {

    @Inject(method = "condition", at = @At("HEAD"))
    private static void setEntityContext(SerializableData.Instance data, Entity entity, CallbackInfoReturnable<Boolean> cir) {
        ModifyTypeTagPower.CURRENT_ENTITY.set(entity);
    }

    @Inject(method = "condition", at = @At("RETURN"))
    private static void clearEntityContext(SerializableData.Instance data, Entity entity, CallbackInfoReturnable<Boolean> cir) {
        ModifyTypeTagPower.CURRENT_ENTITY.remove();
    }
}
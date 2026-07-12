package net.onixary.shapeShifterCurseFabric.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.onixary.shapeShifterCurseFabric.additional_power.ConditionedModifySlipperinessPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class EntitySlipperinessMixin extends Entity {

    public EntitySlipperinessMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyVariable(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isOnGround()Z", ordinal = 2))
    private float modifySlipperiness(float original) {
        Entity entity = this;
	    if (entity instanceof PlayerEntity player) {
		    PowerHolderComponent component = PowerHolderComponent.KEY.get(player);

            for (ConditionedModifySlipperinessPower power : component.getPowers(ConditionedModifySlipperinessPower.class)) {
                if(power.doesApply(getWorld(), getVelocityAffectingPos())) {
                    return original + power.getSlipperinessModifier();
                }
            }
        }
        return  original;
    }

}

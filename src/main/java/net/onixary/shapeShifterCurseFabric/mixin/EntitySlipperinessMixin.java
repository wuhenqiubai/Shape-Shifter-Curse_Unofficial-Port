package net.onixary.shapeShifterCurseFabric.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.additional_power.ConditionedModifySlipperinessPower;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class EntitySlipperinessMixin extends Entity {

    public EntitySlipperinessMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @ModifyVariable(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;onGround()Z", opcode = Opcodes.GETFIELD, ordinal = 2))
    private float modifySlipperiness(float original) {
        Entity entity = (Entity)(Object)this;
        if (entity instanceof Player) {
            Player player = (Player)entity;
		    PowerHolderComponent component = PowerHolderComponent.KEY.get(player);

            for (ConditionedModifySlipperinessPower power : component.getPowers(ConditionedModifySlipperinessPower.class)) {
                if(power.doesApply(level(), getBlockPosBelowThatAffectsMyMovement())) {
                    return original + power.getSlipperinessModifier();
                }
            }
        }
        return  original;
    }

}

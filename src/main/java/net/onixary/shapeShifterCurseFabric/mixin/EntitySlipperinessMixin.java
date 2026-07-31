package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.additional_power.ConditionedModifySlipperinessPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class EntitySlipperinessMixin extends Entity {

    public EntitySlipperinessMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    // 1.21.11: onGround 由字段改为方法(onGround())，且 travel 重构拆分为 travelInAir/travelInFluid/travelFallFlying。
    // 滑腻度(摩擦)在原 travel 中由 `this.onGround ? getSlipperiness() : 1.0F` 计算，
    // 现位于 travelInAir 的 `getBlock().getFriction()`，故改为直接修改 getFriction() 的返回值。
    @ModifyExpressionValue(method = "travelInAir", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getFriction()F"))
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

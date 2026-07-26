package net.onixary.shapeShifterCurseFabric.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.additional_power.HoldBreathPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(LivingEntity.class)
public abstract class HoldBreathMixin extends Entity {
    public HoldBreathMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(at = @At("HEAD"), method = "decreaseAirSupply", cancellable = true)
    private void getNextAirUnderwater(int air, CallbackInfoReturnable<Integer> info) {
        PowerHolderComponent component = PowerHolderComponent.KEY.get(this);


        if (PowerHolderComponent.getPowers(this, HoldBreathPower.class).stream().anyMatch(Power::isActive)) {
            int i = 3;
            int returnValue = this.random.nextInt(i + 1) > 0 ? air : air - 1;
            info.setReturnValue(returnValue);
        }

    }
}


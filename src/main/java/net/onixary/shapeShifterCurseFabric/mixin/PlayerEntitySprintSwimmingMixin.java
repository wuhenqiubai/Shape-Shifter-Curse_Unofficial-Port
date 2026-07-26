package net.onixary.shapeShifterCurseFabric.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.additional_power.AlwaysSprintSwimmingPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Player.class)
public class PlayerEntitySprintSwimmingMixin {

    @ModifyArg(method = "causeFoodExhaustion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;addExhaustion(F)V"), index = 0)
    private float modifySwimmingHungerConsumption(float exhaustion) {
        Player player = (Player) (Object) this;
        if (player.isSwimming()) {
            return PowerHolderComponent.getPowers(player, AlwaysSprintSwimmingPower.class).stream()
                    .map(power -> exhaustion * power.getHungerMultiplier())
                    .findFirst()
                    .orElse(exhaustion);
        }
        return exhaustion;
    }

    @ModifyArg(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"), index = 0)
    private Vec3 modifyVerticalVelocity(Vec3 originalVelocity) {
        Player player = (Player) (Object) this;

        if (PowerHolderComponent.hasPower(player, AlwaysSprintSwimmingPower.class)){
            // 只有在冲刺时才修改Y轴速度
            if (player.isSprinting()) {
                return originalVelocity;
            } else {
                // 保持原有的X和Z速度，但不修改Y速度
                Vec3 currentVelocity = player.getDeltaMovement();
                return new Vec3(originalVelocity.x, currentVelocity.y, originalVelocity.z);
            }
        }
        return originalVelocity;
    }
}

package net.onixary.shapeShifterCurseFabric.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import net.onixary.shapeShifterCurseFabric.additional_power.ModifyFoodHealPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(FoodData.class)
public class HungerManagerMixin {
    @Shadow
    private int tickTimer;

    // 1.21.11: FoodData.tick 参数由 Player 改为 ServerPlayer
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void update(ServerPlayer player, CallbackInfo ci) {
        PowerHolderComponent.getPowers(player, ModifyFoodHealPower.class).forEach(power -> {
            if (power.CanApply(player)) {
                this.tickTimer = power.ProcessFoodTick(this.tickTimer);
            }
        });
    }
}

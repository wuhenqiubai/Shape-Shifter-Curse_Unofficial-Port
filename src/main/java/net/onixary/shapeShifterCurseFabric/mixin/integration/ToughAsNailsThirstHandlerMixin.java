package net.onixary.shapeShifterCurseFabric.mixin.integration;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.integration.toughasnails.ToughAsNailsPowerUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import toughasnails.thirst.ThirstHandler;

@Mixin(value = ThirstHandler.class, remap = false)
public class ToughAsNailsThirstHandlerMixin {
    @WrapOperation(
            method = "onItemUseFinish",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z",
                    remap = true
            ),
            remap = false
    )
    private static boolean shapeShifterCurseFabric$preventDirtyDrinkThirstEffect(Player player, MobEffectInstance effect, Operation<Boolean> original) {
        if (ToughAsNailsPowerUtils.shouldPreventDirtyWaterThirstEffect(player)) {
            return false;
        }
        return original.call(player, effect);
    }
}

package net.onixary.shapeShifterCurseFabric.mixin.integration;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.integration.toughasnails.ToughAsNailsPowerUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import toughasnails.network.DrinkInWorldPacket;

@Mixin(value = DrinkInWorldPacket.class, remap = false)
public class ToughAsNailsDrinkInWorldPacketMixin {
    @WrapOperation(
            method = "lambda$handle$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z",
                    remap = true
            ),
            remap = false
    )
    private static boolean shapeShifterCurseFabric$preventHandDrinkingThirstEffect(Player player, MobEffectInstance effect, Operation<Boolean> original) {
        if (ToughAsNailsPowerUtils.shouldPreventDirtyWaterThirstEffect(player)) {
            return false;
        }
        return original.call(player, effect);
    }
}

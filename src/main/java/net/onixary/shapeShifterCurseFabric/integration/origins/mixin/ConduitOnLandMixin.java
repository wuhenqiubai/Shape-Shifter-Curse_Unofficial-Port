package net.onixary.shapeShifterCurseFabric.integration.origins.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.onixary.shapeShifterCurseFabric.integration.origins.power.OriginsPowerTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ConduitBlockEntity.class)
public class ConduitOnLandMixin {

    @Redirect(method = "applyEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isInWaterOrRain()Z"))
    private static boolean allowConduitPowerOnLand(Player playerEntity) {
        return playerEntity.isInWaterOrRain() || OriginsPowerTypes.CONDUIT_POWER_ON_LAND.isActive(playerEntity);
    }
}
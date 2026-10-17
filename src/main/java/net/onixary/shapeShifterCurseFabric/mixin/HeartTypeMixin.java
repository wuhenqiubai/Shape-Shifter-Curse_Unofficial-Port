package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.additional_power.DisableWitherHeartsPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(targets = "net.minecraft.client.gui.Gui$HeartType")
public class HeartTypeMixin {
    // The second status-effect query is Wither; poison and frozen heart selection stay vanilla.
    @ModifyExpressionValue(method = "forPlayer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;hasEffect(Lnet/minecraft/core/Holder;)Z",
            ordinal = 1))
    private static boolean shape_shifter_curse$hideWitheredHearts(boolean withered, Player player) {
        return withered && !PowerHolderComponent.hasPower(player, DisableWitherHeartsPower.class);
    }
}

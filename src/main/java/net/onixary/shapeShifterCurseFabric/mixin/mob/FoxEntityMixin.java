package net.onixary.shapeShifterCurseFabric.mixin.mob;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.additional_power.FoxFriendlyPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Fox.class)
public class FoxEntityMixin {

    // 1.21.11: Fox.trusts 签名从 trusts(UUID) 改为 trusts(LivingEntity)
    @Inject(method = "trusts", at = @At("HEAD"), cancellable = true)
    private void allowTrustingFriendlyPlayers(LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir) {
        // 如果目标实体是拥有 FoxFriendlyPower 的玩家，则信任该玩家
        if (livingEntity instanceof Player player) {
            if (PowerHolderComponent.hasPower(player, FoxFriendlyPower.class)) {
                cir.setReturnValue(true);
            }
        }
    }
}

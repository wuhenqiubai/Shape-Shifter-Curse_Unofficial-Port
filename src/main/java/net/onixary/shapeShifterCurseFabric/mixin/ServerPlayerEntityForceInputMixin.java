package net.onixary.shapeShifterCurseFabric.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.server.level.ServerPlayer;
import net.onixary.shapeShifterCurseFabric.additional_power.KeepSneakingPower;
import net.onixary.shapeShifterCurseFabric.networking.ModPacketsS2CServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerEntityForceInputMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;

        // 检查玩家是否拥有并激活了 KeepSneakingPower
        boolean shouldForceSneak = PowerHolderComponent.getPowers(player, KeepSneakingPower.class)
                .stream()
                .anyMatch(KeepSneakingPower::isActive);
        // 发送状态到客户端
        ModPacketsS2CServer.sendForceSneakState(player, shouldForceSneak);
    }

}

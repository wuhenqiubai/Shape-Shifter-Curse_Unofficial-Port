package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "restoreFrom", at = @At("RETURN"))
    private void ssc$copySpawnPoint(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        // 1.21.1 的 copyFrom 不复制重生点（spawnPointPosition/spawnPointDimension/spawnForced/spawnAngle）
        // Apoli 的 @WrapWithCondition 在 respawnPlayer 中也可能跳过 setSpawnPointFrom。
        // 手动复制重生点，保证不论 Apoli 是否跳过，重生点都能传到新玩家。
        ServerPlayer self = (ServerPlayer)(Object)this;
        self.setRespawnPosition(
                oldPlayer.getRespawnDimension(),
                oldPlayer.getRespawnPosition(),
                oldPlayer.getRespawnAngle(),
                oldPlayer.isRespawnForced(),
                false
        );
    }
}

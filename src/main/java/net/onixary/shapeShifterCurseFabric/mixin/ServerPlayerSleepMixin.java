package net.onixary.shapeShifterCurseFabric.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.onixary.shapeShifterCurseFabric.status_effects.attachment.EffectManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerSleepMixin {

    @Inject(method = "startSleepInBed", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;bedBlocked(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z", shift = At.Shift.AFTER), cancellable = true)
    private void allowSleepWithTransformEffect(BlockPos pos, CallbackInfoReturnable<Either<BedSleepingProblem, Unit>> cir) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (EffectManager.hasTransformativeEffect(player)) {
            player.startSleeping(pos);
            player.awardStat(net.minecraft.stats.Stats.SLEEP_IN_BED);
            cir.setReturnValue(Either.right(Unit.INSTANCE));
        }
    }
}

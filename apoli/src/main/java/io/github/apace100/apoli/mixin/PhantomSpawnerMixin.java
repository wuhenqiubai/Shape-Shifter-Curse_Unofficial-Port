package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyInsomniaTicksPower;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PhantomSpawner.class)
public class PhantomSpawnerMixin {

    @Unique
    private Player apoli$CachedPlayer;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/dimension/DimensionType;hasSkyLight()Z", ordinal = 1))
    private void cachePlayerEntity(ServerLevel level, boolean spawnEnemies, boolean spawnFriendlies, CallbackInfoReturnable<Integer> cir, @Local ServerPlayer serverPlayerEntity) {
        apoli$CachedPlayer = serverPlayerEntity;
    }

    @ModifyVariable(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I", ordinal = 1), ordinal = 1)
    private int modifyTicks(int original) {
        return (int)PowerHolderComponent.modify(apoli$CachedPlayer, ModifyInsomniaTicksPower.class, original);
    }
}

package io.github.apace100.apoli.mixin.integration.connector;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * NeoForge/Connector 兼容：主 PhantomSpawnerMixin 的 ModifyInsomniaTicksPower 用
 * {@code @ModifyVariable(method = "tick", target = "RandomSource.nextInt")} 修改局部 int（ordinal=1），
 * NeoForge 重编译使局部变量顺序可能变化而失效。此版本参考 Apoli 2.9.2 connector：仅缓存当前 ServerPlayer
 * 到 {@link #apoli$CachedPlayer}（供 ModifyInsomniaTicksPower 后续使用）；@ModifyVariable 在 NeoForge 下放弃
 * （2.9.2 源码中为注释掉的 TODO），connector 版不实现。由 ApoliMixinPlugin 在 NeoForge 下启用、Fabric 下跳过。
 */
@Mixin(PhantomSpawner.class)
public class PhantomSpawnerMixin {

    @Unique
    private Player apoli$CachedPlayer;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/dimension/DimensionType;hasSkyLight()Z", ordinal = 1))
    private void cachePlayerEntity(ServerLevel level, boolean spawnEnemies, boolean spawnFriendlies, CallbackInfoReturnable<Integer> cir, @Local ServerPlayer serverPlayerEntity) {
        apoli$CachedPlayer = serverPlayerEntity;
    }
}

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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 主包 {@code PhantomSpawnerMixin} 的 Connector 替代版（对应 1.21.1 提交 ea98804）。
 *
 * <p>主版 {@code @ModifyVariable(method = "tick", ... ordinal = 0)} 改「幻翼生成倒计时」这个局部 int，
 * NeoForge 重编译后局部变量顺序不稳定，会静默失效。
 *
 * <p>⚠ <b>这是有意的功能降级</b>：本版只保留缓存当前 {@link ServerPlayer}，
 * 不再改倒计时 —— 即 NeoForge/Connector 环境下 {@code modify_insomnia_ticks} power 不生效
 * （1.21.1 的 connector 版同样如此，原注释标为 TODO）。Fabric 侧不受影响，主版照常工作。
 *
 * <p>由 {@link io.github.apace100.apoli.ApoliMixinPlugin} 在 Connector 环境下启用、Fabric 下跳过。
 */
@Mixin(PhantomSpawner.class)
public class PhantomSpawnerMixin {

    @Unique
    private Player apoli$CachedPlayer;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/dimension/DimensionType;hasSkyLight()Z", ordinal = 1))
    private void cachePlayerEntity(ServerLevel level, boolean spawnEnemies, CallbackInfo ci, @Local ServerPlayer serverPlayerEntity) {
        apoli$CachedPlayer = serverPlayerEntity;
    }
}

package io.github.apace100.apoli.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * NeoForge/Connector 兼容（对应 1.21.1 提交 ea98804）：在
 * {@link net.minecraft.server.level.ServerPlayerGameMode#destroyBlock} 与
 * {@link net.minecraft.world.entity.player.Player#hasCorrectToolForDrops} 之间传递当前破坏方块上下文。
 *
 * <p>Fabric 主版在 {@code ServerPlayerInteractionManagerMixin} 里用实例字段 + {@code @ModifyVariable} 处理
 * {@code ModifyHarvestPower}；NeoForge 重编译后局部变量顺序不稳定，{@code @ModifyVariable(ordinal = 1)} 会失效。
 * 故沿用 Apoli 2.9.2 connector 的 ThreadLocal 方案：破坏方块入口 HEAD 缓存方块位置与工具判定结果，
 * connector 的 {@code PlayerEntityMixin} 在 {@code hasCorrectToolForDrops} HEAD 读取该缓存。
 *
 * <p>参考：Apoli 2.9.2 {@code util/HarvestContext.java}（yarn 名），此处为 mojmap 适配。
 * 两侧 mixin 由 {@link io.github.apace100.apoli.ApoliMixinPlugin} 在 Connector 环境下启用、Fabric 下跳过。
 */
public class HarvestContext {

    private static final ThreadLocal<SavedBlockPosition> BLOCK_POSITION = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> CAN_HARVEST = new ThreadLocal<>();

    public static void setBlockPosition(ServerLevel world, BlockPos pos) {
        BLOCK_POSITION.set(new SavedBlockPosition(world, pos));
    }

    public static SavedBlockPosition getBlockPosition() {
        return BLOCK_POSITION.get();
    }

    public static void clearBlockPosition() {
        BLOCK_POSITION.remove();
    }

    public static void setCanHarvest(Boolean bool) {
        CAN_HARVEST.set(bool);
    }

    public static Boolean getCanHarvest() {
        return CAN_HARVEST.get();
    }

    public static void clearCanHarvest() {
        CAN_HARVEST.remove();
    }
}

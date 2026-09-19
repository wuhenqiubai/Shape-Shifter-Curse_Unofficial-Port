package io.github.apace100.apoli.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * NeoForge/Connector 兼容：在 {@link net.minecraft.server.level.ServerPlayerGameMode#destroyBlock}（mojmap，
 * 即 yarn tryBreakBlock）与 {@link net.minecraft.world.entity.player.Player#hasCorrectToolForDrops}（mojmap，
 * 即 yarn canHarvest）之间传递当前破坏方块上下文。
 *
 * <p>Fabric 主版在 ServerPlayerInteractionManagerMixin 里用实例字段 + @ModifyVariable 处理 ModifyHarvestPower；
 * NeoForge 重编译后 @ModifyVariable 的 ordinal/局部变量不稳定，故沿用 Apoli 2.9.2 connector 的 ThreadLocal 方案：
 * 破坏方块入口 HEAD 缓存方块位置与工具判定结果，PlayerEntityMixin 的 hasCorrectToolForDrops HEAD 读取该缓存。</p>
 *
 * <p>参考：Apoli 2.9.2 {@code util/HarvestContext.java}（yarn 名），此处为 1.21.1 mojmap 适配。</p>
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

package net.onixary.shapeShifterCurseFabric.util.util.cost;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IFUSDrawableCostType<T extends ICostType<T>> extends ICostType<T> {
    // 1s查询一次 如果getAmountText还是对性能影响较大 可以改成-1来禁用自动查询 仅在需要更新数据时查询(比如切换选中Perk)
    public default int checkInterval() {
        return 20;
    }

    public void drawIcon(GuiGraphics context, @NotNull ICost costObject, @Nullable Player player, int x, int y, int z);

    public default Component getAmountText(@NotNull ICost costObject, @Nullable Player player) {
        return Component.nullToEmpty(String.valueOf(costObject.getAmount()));
    }

    public default Component getResourceNotEnoughText(@NotNull ICost costObject, @Nullable Player player) {
        return Component.translatable("gui.shape-shifter-curse.perk.unavailable");
    }
}

package net.onixary.shapeShifterCurseFabric.util.util.cost;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IFUSDrawableCostType<T extends ICostType<T>> extends ICostType<T> {

    public default Component getAmountText(@NotNull ICost costObject, @Nullable Player player) {
        return Component.nullToEmpty(String.valueOf(costObject.getAmount()));
    }
}

package net.onixary.shapeShifterCurseFabric.util.util.cost;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ICostType<T extends ICostType<T>> {
    public Identifier getID();

    public boolean canPay(@NotNull ICost costObject, @Nullable Player player);

    public default boolean canPay_CLIENT(@NotNull ICost costObject, @Nullable Player player) {
        return canPay(costObject, player);
    }

    public void pay(@NotNull ICost costObject, @NotNull Player player);
}

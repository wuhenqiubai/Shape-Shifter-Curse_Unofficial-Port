package net.onixary.shapeShifterCurseFabric.magic;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.util.util.ISprite;
import org.jetbrains.annotations.NotNull;

public interface IMagic {
    public Identifier getId();

    public ISprite getSprite();

    public default ISprite getUnselectedSprite() {
        return getSprite();
    }

    // 这几个个不准备在这个版本实现 留给拓展实现了 等拓展验证完成后再加入主线
    public default void onInvokeCommon(@NotNull Player player) { }

    public default void onInvokeServer(@NotNull Player player) {
        this.onInvokeCommon(player);
    }

    public default void onInvokeClient(@NotNull Player player) {
        this.onInvokeCommon(player);
    }

    public default boolean canInvoke(@NotNull Player player) {
        return true;
    }

    public default boolean virtualInvoke() { return false; }

    public default int getCooldown() {
        return 0;
    }
}

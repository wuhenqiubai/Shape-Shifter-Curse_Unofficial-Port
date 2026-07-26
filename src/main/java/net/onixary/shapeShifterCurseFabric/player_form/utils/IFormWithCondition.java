package net.onixary.shapeShifterCurseFabric.player_form.utils;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public interface IFormWithCondition {
    default boolean checkCanUse(@Nullable Player player) {
        return true;
    }
}

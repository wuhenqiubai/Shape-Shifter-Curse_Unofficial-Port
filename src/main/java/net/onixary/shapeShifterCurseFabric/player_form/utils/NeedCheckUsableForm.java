package net.onixary.shapeShifterCurseFabric.player_form.utils;

import net.minecraft.world.entity.player.Player;

public interface NeedCheckUsableForm {
    default boolean IsPlayerCanUse(Player player) {
        return true;
    }
}

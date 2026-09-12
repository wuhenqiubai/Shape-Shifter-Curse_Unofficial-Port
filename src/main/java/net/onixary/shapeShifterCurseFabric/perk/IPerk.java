package net.onixary.shapeShifterCurseFabric.perk;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;

// Server Side
public interface IPerk {
    ResourceLocation getID();

    default void onGain(Player player, IForm form) {
        this.onLoad(player, form);
    }

    default boolean canGain(Player player, IForm form) {
        return true;  // Tier 和 DependentPerkID 的判定由 PerkTree 处理
    }

    default void onLoad(Player player, IForm form) { }
}

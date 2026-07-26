package net.onixary.shapeShifterCurseFabric.player_form.utils;

import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.util.Verify.PatronDataSegment;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface IPatronForm {
    default boolean checkCanUse(@Nullable Player player, @Nullable UUID playerUUID, @Nullable PatronDataSegment patronData) {
        return true;
    }
}

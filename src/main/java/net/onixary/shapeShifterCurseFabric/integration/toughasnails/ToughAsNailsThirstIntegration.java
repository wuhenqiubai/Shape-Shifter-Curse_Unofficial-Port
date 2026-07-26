package net.onixary.shapeShifterCurseFabric.integration.toughasnails;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import toughasnails.api.thirst.IThirst;
import toughasnails.api.thirst.ThirstHelper;
import toughasnails.thirst.ThirstHandler;

public class ToughAsNailsThirstIntegration {
    public static void addThirst(Player player, int amount) {
        IThirst thirst = ThirstHelper.getThirst(player);
        int modifiedThirst = Math.max(0, Math.min(20, thirst.getThirst() + amount));
        thirst.setThirst(modifiedThirst);

        if (player instanceof ServerPlayer serverPlayer) {
            ThirstHandler.syncThirst(serverPlayer);
        }
    }
}

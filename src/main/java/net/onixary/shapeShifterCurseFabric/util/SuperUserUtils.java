package net.onixary.shapeShifterCurseFabric.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.networking.ModPacketsS2CServer;
import net.onixary.shapeShifterCurseFabric.util.util.CachedDataMap;

import java.util.UUID;

public class SuperUserUtils {
    // 用于在测试环境内使用命令 IDEA的服务器环境我之前用的是rcon给的OP 有点麻烦 还是加一个su命令吧 需要"enableDebugCommand==true"
    private static final CachedDataMap<UUID, Player, Integer> superUserData = new CachedDataMap<>(player -> -1, Entity::getUUID);
    private static int clientSuperUserLevel = -1;

    public static int getCurrentPermissionLevel(Player player) {
        return superUserData.get(player);
    }

    public static int getCurrentPermissionLevel(UUID playerUuid) {
        return superUserData.get(playerUuid, null);
    }

    public static int getClientPermissionLevel() {
        return clientSuperUserLevel;
    }

    public static void setSULevel(ServerPlayer player, int level) {
        if (level < -1 || level > 4) {
            throw new IllegalArgumentException("level must be between -1 and 4");
        }
        superUserData.setA(player, level);
        ModPacketsS2CServer.sendSetSuperUserLevel(player, level);
    }

    public static void setClientSULevel(int level) {
        if (level < -1 || level > 4) {
            throw new IllegalArgumentException("level must be between -1 and 4");
        }
        clientSuperUserLevel = level;
    }
}

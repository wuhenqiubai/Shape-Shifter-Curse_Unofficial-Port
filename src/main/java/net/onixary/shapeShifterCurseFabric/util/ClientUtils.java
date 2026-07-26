package net.onixary.shapeShifterCurseFabric.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

// 服务端不能访问这个class里的任何函数 有随机崩溃问题 所以现在所有函数加了一个检查 防止测试时测不出问题 发布时随机崩溃
public class ClientUtils {
    public static boolean isOpenInventoryScreen = false;

    public static Player getPlayer() {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
            throw new RuntimeException("Cannot invoke this method in a non-client environment");
            // return null;
        }
        return Minecraft.getInstance().player;
    }

    public static boolean CanDisplayGUI() {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
            throw new RuntimeException("Cannot invoke this method in a non-client environment");
            // return true;
        }
        return !Minecraft.getInstance().options.hideGui;
    }

    public static @Nullable UUID getPlayerUUID() {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
            throw new RuntimeException("Cannot invoke this method in a non-client environment");
        }
        return Minecraft.getInstance().getUser().getProfileId();
    }
}

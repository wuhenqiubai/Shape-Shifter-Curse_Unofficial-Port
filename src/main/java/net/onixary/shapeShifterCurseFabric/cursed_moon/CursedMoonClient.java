package net.onixary.shapeShifterCurseFabric.cursed_moon;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import static net.onixary.shapeShifterCurseFabric.cursed_moon.CursedMoon.isCursedMoonDay;

public class CursedMoonClient {
    // Client Side
    public static boolean isCursedMoon = false;  // 由同步包更新
    public static boolean middayMessageSent = false;  // 接收到同步包时自动置为 false

    public static void clientTick(Level world) {
        if (!isCursedMoonDay(world)) { return; }
        long dayTime = world.getDayTime() % 24000;
        if (dayTime >= 6000L && dayTime < 12500L && !middayMessageSent) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                if (player.level().dimension() != Level.OVERWORLD) {
                    player.displayClientMessage(Component.translatable("info.shape-shifter-curse.before_cursed_moon_nether").withStyle(ChatFormatting.LIGHT_PURPLE), false);
                } else {
                    player.displayClientMessage(Component.translatable("info.shape-shifter-curse.before_cursed_moon").withStyle(ChatFormatting.LIGHT_PURPLE), false);
                }
            }
            middayMessageSent = true;
        }
    }
}
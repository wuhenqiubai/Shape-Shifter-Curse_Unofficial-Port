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
    public static boolean middayMessageSent = false;
    private static long middayMessageDay = Long.MIN_VALUE;

    public static void clientTick(Level world) {
        if (!isCursedMoonDay(world)) { return; }
        // 26.1：getDayTime 已删除，Level#getOverworldClockTime 是等价替代（客户端走 ClientClockManager）
        long clockTime = world.getOverworldClockTime();
        long day = clockTime / 24000;
        long dayTime = clockTime % 24000;
        if (day != middayMessageDay) {
            middayMessageDay = day;
            middayMessageSent = false;
        }
        if (dayTime >= 6000L && dayTime < 12500L && !middayMessageSent) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                if (player.level().dimension() != Level.OVERWORLD) {
                    player.sendSystemMessage(Component.translatable("info.shape-shifter-curse.before_cursed_moon_nether").withStyle(ChatFormatting.LIGHT_PURPLE));
                } else {
                    player.sendSystemMessage(Component.translatable("info.shape-shifter-curse.before_cursed_moon").withStyle(ChatFormatting.LIGHT_PURPLE));
                }
            }
            middayMessageSent = true;
        }
    }
}
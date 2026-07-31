package net.onixary.shapeShifterCurseFabric.cursed_moon;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.event.SSCEvent;
import net.onixary.shapeShifterCurseFabric.networking.ModPacketsS2CServer;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.ITransformReason;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;
import net.onixary.shapeShifterCurseFabric.player_form.utils.PlayerFormComponent;
import net.onixary.shapeShifterCurseFabric.player_form.utils.TransformManager;

import java.util.Arrays;
import java.util.Optional;

public class CursedMoon {
    // Server Side
    public static long day = -1;  // 用于实现进入新的日期时自动同步诅咒之月 要不是CommonConfig客户端和服务器端可能不同 连同步都不需要

    public static boolean isCursedMoonByPhase(int moonPhase) {
        int[] curseMoonPhase = ShapeShifterCurseFabric.commonConfig.curseMoonPhase;
        return Arrays.stream(curseMoonPhase).anyMatch(phase -> phase == moonPhase);
    }

    public static boolean isCursedMoonDay(Level world) {
        if (world.isClientSide()) {
            return CursedMoonClient.isCursedMoon;
        }
        int moonPhase = world.environmentAttributes().getValue(net.minecraft.world.attribute.EnvironmentAttributes.MOON_PHASE, net.minecraft.core.BlockPos.ZERO).index();
        return isCursedMoonByPhase(moonPhase);
    }

    public static boolean isNight(Level world) {
        long timeDayMoon = world.getDayTime() % 24000;
        return timeDayMoon > 12000L && timeDayMoon < 23000L;
    }

    public static boolean isInCursedMoon(Level world) {
        return isCursedMoonDay(world) && isNight(world);
    }

    public static void applyStartCursedMoonEffect(Level world, Player player) {
        // java16+ 真神奇的写法
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (PlayerFormComponent.COMPONENT.get(player).isCursedMoonApplied) {
            return;
        }
        boolean isOverworld = player.level().dimension() == Level.OVERWORLD;
        if (RegPlayerForms.ORIGINAL_BEFORE_ENABLE.isPlayerForm(player)) {
            if (isOverworld) {
                serverPlayer.sendSystemMessage(Component.translatable("info.shape-shifter-curse.on_cursed_moon_before_enable").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        } else {
            if(isOverworld){
                serverPlayer.sendSystemMessage(Component.translatable("info.shape-shifter-curse.on_cursed_moon").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
            else{
                serverPlayer.sendSystemMessage(Component.translatable("info.shape-shifter-curse.on_cursed_moon_nether").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
            ShapeShifterCurseFabric.ON_TRIGGER_CURSED_MOON.trigger(serverPlayer);
        }
        PlayerFormComponent component = PlayerFormComponent.COMPONENT.get(player);
        component.isCursedMoonApplied = true;
        component.lastTransformByCure = false;
        component.BeforeCursedMoonAppliedForm = null;
        component.AfterCursedMoonAppliedForm = null;
        if (!RegPlayerForms.ORIGINAL_BEFORE_ENABLE.isPlayerForm(player) && ShapeShifterCurseFabric.commonConfig.enableCursedMoonTransform) {
            IForm nowForm = component.nowForm;
            IForm targetForm = component.nowForm._getNextForm(player, ITransformReason.CursedMoon);
            if (!nowForm.isEquals(targetForm)) {
                component.BeforeCursedMoonAppliedForm = nowForm;
                component.AfterCursedMoonAppliedForm = targetForm;
                TransformManager.startTransform(player, targetForm, null);
                if (FormUtils.CursedMoonFinalForm.hasFlag(nowForm)) {
                    ShapeShifterCurseFabric.ON_TRIGGER_CURSED_MOON_FORM_2.trigger(serverPlayer);
                }
            }
        }
        component.sync();
        SSCEvent.CURSED_MOON_BEGIN.invoker().onEvent(player);
    }

    public static void applyEndCursedMoonEffect(Level world, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (!PlayerFormComponent.COMPONENT.get(player).isCursedMoonApplied) {
            return;
        }
        boolean isOverworld = player.level().dimension() == Level.OVERWORLD;
        PlayerFormComponent component = PlayerFormComponent.COMPONENT.get(player);
        if (RegPlayerForms.ORIGINAL_BEFORE_ENABLE.isPlayerForm(player)) {
            if (isOverworld) {
                ((ServerPlayer)player).sendSystemMessage(Component.translatable("info.shape-shifter-curse.end_cursed_moon_before_enable").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        } else {
            // 要不是可以用别的手段降级(比如我拓展的幻形石) 我直接查当前形态与AfterCursedMoonAppliedForm的等级差就行
            if (component.lastTransformByCure) {
                ((ServerPlayer)player).sendSystemMessage(Component.translatable("info.shape-shifter-curse.end_cursed_moon_by_cure").withStyle(ChatFormatting.LIGHT_PURPLE));
                ShapeShifterCurseFabric.ON_END_CURSED_MOON_CURED.trigger(serverPlayer);
                if (component.AfterCursedMoonAppliedForm != null && component.AfterCursedMoonAppliedForm.getFormTier() == 1) {
                    ShapeShifterCurseFabric.ON_END_CURSED_MOON_CURED_FORM_2.trigger(serverPlayer);
                }
            } else if(RegPlayerForms.ORIGINAL_SHIFTER.isPlayerForm(player)){
                ((ServerPlayer)player).sendSystemMessage(Component.translatable("info.shape-shifter-curse.end_cursed_moon_special").withStyle(ChatFormatting.LIGHT_PURPLE));
            } else if (component.BeforeCursedMoonAppliedForm != null && component.AfterCursedMoonAppliedForm != null && component.AfterCursedMoonAppliedForm.isPlayerForm(player)) {
                ((ServerPlayer)player).sendSystemMessage(Component.translatable("info.shape-shifter-curse.end_cursed_moon").withStyle(ChatFormatting.LIGHT_PURPLE));
                ShapeShifterCurseFabric.ON_END_CURSED_MOON.trigger(serverPlayer);
            }
        }
        component.isCursedMoonApplied = false;
        component.lastTransformByCure = false;
        IForm targetForm = component.nowForm._getPrevForm(player, ITransformReason.CursedMoon);
        TransformManager.startTransform(player, targetForm, null);
        component.BeforeCursedMoonAppliedForm = null;
        component.AfterCursedMoonAppliedForm = null;
        component.sync();
        SSCEvent.CURSED_MOON_END.invoker().onEvent(player);
    }

    public static void serverTick(MinecraftServer minecraftServer) {
        Level world = minecraftServer.getLevel(Level.OVERWORLD);
        if (world.isClientSide()) return;
        long timeOfDay = world.getDayTime();
        long nowDay = timeOfDay / 24000;
        long dayTime = timeOfDay % 24000;
        if (nowDay != day) {
            day = nowDay;
            for (Player player : minecraftServer.getPlayerList().getPlayers()) {
                if (player instanceof ServerPlayer serverPlayer) {
                    ModPacketsS2CServer.sendCursedMoonData(serverPlayer, isCursedMoonDay(world));
                }
            }
        }
        if (isInCursedMoon(world)) {
            for (Player player : minecraftServer.getPlayerList().getPlayers()) {
                if(player.isSleeping() && !ShapeShifterCurseFabric.commonConfig.allowSleepInCursedMoon){
                    player.stopSleeping();
                }
                applyStartCursedMoonEffect(world, player);
            }
        } else {
            for (Player player : minecraftServer.getPlayerList().getPlayers()) {
                applyEndCursedMoonEffect(world, player);
            }
        }
    }

    public static Optional<Integer> getNextCurseMoonPhase(int NowPhase) {
        int MoonPhaseCount = 8;
        for (int DaySkip = 0; DaySkip < MoonPhaseCount; DaySkip++) {
            int CurrentPhase = (NowPhase + DaySkip) % MoonPhaseCount;
            if (isCursedMoonByPhase(CurrentPhase)) {
                return Optional.of(CurrentPhase);
            }
        }
        return Optional.empty();
    }

    public static void forceTriggerCursedMoon(ServerLevel world) {
        int currentPhase = world.environmentAttributes().getValue(net.minecraft.world.attribute.EnvironmentAttributes.MOON_PHASE, net.minecraft.core.BlockPos.ZERO).index();
        Optional<Integer> nextCursedPhase = getNextCurseMoonPhase(currentPhase);
        if (nextCursedPhase.isEmpty()) {
            ShapeShifterCurseFabric.LOGGER.warn("Cannot trigger CursedMoon: no next cursed phase found");
            return;
        }
        int targetPhase = nextCursedPhase.get();

        // 计算需要跳过的天数来达到目标月相
        int daysToSkip = (targetPhase - currentPhase + 8) % 8;
        if (daysToSkip == 0) daysToSkip = 8; // 如果已经是诅咒月相，跳到下一个

        // 调整世界时间到目标月相
        long currentTime = world.getDayTime();
        long newTime = currentTime + (daysToSkip * 24000L);
        world.setDayTime(newTime);

        ShapeShifterCurseFabric.LOGGER.info("CursedMoon manually triggered! Skipped " + daysToSkip + " days to reach moon phase " + targetPhase);

        // 向所有玩家发送消息
        for (ServerPlayer player : world.getServer().getPlayerList().getPlayers()) {
            if (!RegPlayerForms.ORIGINAL_BEFORE_ENABLE.isPlayerForm(player)) {
                ((ServerPlayer)player).sendSystemMessage(Component.translatable("info.shape-shifter-curse.cursed_moon_forced").withStyle(ChatFormatting.DARK_PURPLE));
            }
        }

        // 立即向所有在线玩家同步状态
        boolean currentIsNight = isNight(world);
        for (ServerPlayer player : world.getServer().getPlayerList().getPlayers()) {
            ModPacketsS2CServer.sendCursedMoonData(player, true);
        }
    }
}
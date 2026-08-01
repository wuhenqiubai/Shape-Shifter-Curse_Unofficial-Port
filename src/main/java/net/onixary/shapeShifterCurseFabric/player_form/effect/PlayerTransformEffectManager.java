package net.onixary.shapeShifterCurseFabric.player_form.effect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.onixary.shapeShifterCurseFabric.networking.ModPacketsS2CServer;

public class PlayerTransformEffectManager {

    public static void applyStartTransformEffect(ServerPlayer player, int duration) {
        // add darkness effect
        MobEffectInstance darknessEffect = new MobEffectInstance(MobEffects.BLINDNESS, duration);
        player.addEffect(darknessEffect);

        ModPacketsS2CServer.sendNoMoveTick(player, duration);
        ModPacketsS2CServer.sendNoJumpTick(player, duration);
    }

    public static void applyEndTransformEffect(ServerPlayer player, int duration) {
        // add nausea effect
        MobEffectInstance nauseaEffect = new MobEffectInstance(MobEffects.CONFUSION, duration);
        player.addEffect(nauseaEffect);

        ModPacketsS2CServer.sendNoMoveTick(player, duration);
        ModPacketsS2CServer.sendNoJumpTick(player, duration);

    }

    public static void applyFinaleTransformEffect(ServerPlayer player, int duration){
        ModPacketsS2CServer.sendNoMoveTick(player, duration);
    }
}
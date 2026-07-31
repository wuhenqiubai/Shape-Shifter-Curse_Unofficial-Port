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

        // add immobility effect
        MobEffectInstance immobilityEffect = new MobEffectInstance(MobEffects.SLOWNESS, duration, 245);
        player.addEffect(immobilityEffect);

	    ModPacketsS2CServer.sendNoJumpTick(player, duration);
    }

    public static void applyEndTransformEffect(ServerPlayer player, int duration) {
        // add nausea effect
        MobEffectInstance nauseaEffect = new MobEffectInstance(MobEffects.NAUSEA, duration);
        player.addEffect(nauseaEffect);

        // add immobility effect
        MobEffectInstance immobilityEffect = new MobEffectInstance(MobEffects.SLOWNESS, duration, 245);
        player.addEffect(immobilityEffect);

	    ModPacketsS2CServer.sendNoJumpTick(player, duration);
    }

    public static void applyFinaleTransformEffect(ServerPlayer player, int duration){

        // slowness effect remain some time
        MobEffectInstance immobilityEffect = new MobEffectInstance(MobEffects.SLOWNESS, duration, 200);
        player.addEffect(immobilityEffect);

    }
}

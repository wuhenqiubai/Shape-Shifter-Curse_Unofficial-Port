package io.github.apace100.apoli.util;

import io.github.apace100.apoli.networking.SyncStatusEffectPacket;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

public class SyncStatusEffectsUtil {

    public static void sendStatusEffectUpdatePacket(LivingEntity living, UpdateType type, MobEffectInstance instance) {
        if (living.level().isClientSide()) return;

        for (ServerPlayer player : PlayerLookup.tracking(living)) {
            ServerPlayNetworking.send(player, new SyncStatusEffectPacket(living.getId(), (byte) type.ordinal(), type == UpdateType.CLEAR ? Optional.empty() : Optional.of(instance)));
        }
    }

    public enum UpdateType {
        CLEAR, APPLY, UPGRADE, REMOVE
    }
}
package io.github.apace100.apoli.networking;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.apoli.util.SyncStatusEffectsUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketSendListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModPacketsS2C {

    @Environment(EnvType.CLIENT)
    public static void register() {
        ClientLoginNetworking.registerGlobalReceiver(ModPackets.HANDSHAKE, ModPacketsS2C::handleHandshake);
        ClientPlayConnectionEvents.INIT.register(((clientPlayNetworkHandler, minecraftClient) -> {
            ClientPlayNetworking.registerReceiver(ModPackets.POWER_LIST, ModPacketsS2C::receivePowerList);
            ClientPlayNetworking.registerReceiver(ModPackets.SYNC_POWER, ModPacketsS2C::onPowerSync);
            ClientPlayNetworking.registerReceiver(ModPackets.PLAYER_MOUNT, ModPacketsS2C::onPlayerMount);
            ClientPlayNetworking.registerReceiver(ModPackets.PLAYER_DISMOUNT, ModPacketsS2C::onPlayerDismount);
            ClientPlayNetworking.registerReceiver(ModPackets.SET_ATTACKER, ModPacketsS2C::onSetAttacker);
            ClientPlayNetworking.registerReceiver(ModPackets.SYNC_STATUS_EFFECT, ModPacketsS2C::onStatusEffectSync);
        }));
    }

    private static void onStatusEffectSync(SyncStatusEffectPacket payload, ClientPlayNetworking.Context context) {
        int targetId = payload.entityId();
        SyncStatusEffectsUtil.UpdateType updateType = SyncStatusEffectsUtil.UpdateType.values()[payload.updateType()];
        MobEffectInstance finalInstance = payload.effectInstance().orElse(null);
        context.client().execute(() -> {
            Entity target = context.player().level().getEntity(targetId);
            if (!(target instanceof LivingEntity living)) {
                Apoli.LOGGER.warn("Received unknown target for status effect synchronization");
            } else {
                switch(updateType) {
                    case CLEAR -> living.getActiveEffectsMap().clear();
                    case APPLY, UPGRADE -> living.getActiveEffectsMap().put(finalInstance.getEffect(), finalInstance);
                    case REMOVE -> living.getActiveEffectsMap().remove(finalInstance.getEffect());
                }
            }
        });
    }

    private static void onSetAttacker(SetAttackerPacket payload, ClientPlayNetworking.Context context) {
        int targetId = payload.entityId();
        boolean hasAttacker = payload.attackingEntityId().isPresent();
        int attackerId = 0;
        if(hasAttacker) {
            attackerId = payload.attackingEntityId().orElseThrow();
        }
        int finalAttackerId = attackerId;
        context.client().execute(() -> {
            Entity target = context.player().level().getEntity(targetId);
            Entity attacker = null;
            if(hasAttacker) {
                attacker = context.player().level().getEntity(finalAttackerId);
            }
            if (!(target instanceof LivingEntity)) {
                Apoli.LOGGER.warn("Received unknown target");
            } else if(hasAttacker && !(attacker instanceof LivingEntity)) {
                Apoli.LOGGER.warn("Received unknown attacker");
            } else {
                if(hasAttacker) {
                    ((LivingEntity)target).setLastHurtByMob((LivingEntity)attacker);
                } else {
                    ((LivingEntity)target).setLastHurtByMob(null);
                }
            }
        });
    }


    @Environment(EnvType.CLIENT)
    private static CompletableFuture<FriendlyByteBuf> handleHandshake(Minecraft client, ClientHandshakePacketListenerImpl handler, FriendlyByteBuf receivedBuf, Consumer<PacketSendListener> callbacksConsumer) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(Apoli.SEMVER.length);
        for(int i = 0; i < Apoli.SEMVER.length; i++) {
            buf.writeInt(Apoli.SEMVER[i]);
        }
        return CompletableFuture.completedFuture(buf);
    }

    @Environment(EnvType.CLIENT)
    private static void receivePowerList(PowerListPacket payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            PowerTypeRegistry.clear();
            payload.factories().forEach(PowerTypeRegistry::register);
        });
    }

    @Environment(EnvType.CLIENT)
    private static void onPlayerMount(PlayerMountPacket payload, ClientPlayNetworking.Context context) {
        int mountingPlayerId = payload.ridingEntity();
        int mountedPlayerId = payload.vehicleEntity();
        context.client().execute(() -> {
            Entity mountingPlayer = context.player().level().getEntity(mountingPlayerId);
            Entity mountedPlayer = context.player().level().getEntity(mountedPlayerId);
            if (mountedPlayer == null) {
                Apoli.LOGGER.warn("Received passenger for unknown player");
            } else if(mountingPlayer == null) {
                Apoli.LOGGER.warn("Received unknown passenger for player");
            } else {
                boolean result = mountingPlayer.startRiding(mountedPlayer, true);
                if(result) {
                    Apoli.LOGGER.info(mountingPlayer.getDisplayName().getString() + " started riding " + mountedPlayer.getDisplayName().getString());
                } else {
                    Apoli.LOGGER.warn(mountingPlayer.getDisplayName().getString() + " failed to start riding " + mountedPlayer.getDisplayName().getString());
                }
            }
        });
    }

    @Environment(EnvType.CLIENT)
    private static void onPlayerDismount(PlayerDismountPacket payload, ClientPlayNetworking.Context context) {
        int dismountingPlayerId = payload.ridingEntity();
        context.client().execute(() -> {
            Entity dismountingPlayer = context.player().level().getEntity(dismountingPlayerId);
            if (dismountingPlayer == null) {
                Apoli.LOGGER.warn("Unknown player tried to dismount");
            } else {
                if(dismountingPlayer.getVehicle() instanceof Player) {
                    dismountingPlayer.removeVehicle();
                }
            }
        });
    }

    @Environment(EnvType.CLIENT)
    private static void onPowerSync(SyncPowerPacket payload, ClientPlayNetworking.Context context) {
        int entityId = payload.entityId();
        ResourceLocation powerId = payload.powerId();
        CompoundTag powerNbtContainer = payload.powerNbtContainer();
        Tag powerNbt = powerNbtContainer.get("Data");
        context.client().execute(() -> {
            if(!PowerTypeRegistry.contains(powerId)) {
                Apoli.LOGGER.warn("Received sync packet for unknown power type: " + powerId);
                return;
            }
            Entity entity = context.player().level().getEntity(entityId);
            if (entity == null) {
                Apoli.LOGGER.warn("Received sync packet for unknown power holder.");
                return;
            }
            PowerType<?> powerType = PowerTypeRegistry.get(powerId);
            PowerHolderComponent.KEY.maybeGet(entity).ifPresentOrElse(phc -> {
                Power power = phc.getPower(powerType);
                if (power == null) {
                    // 单机（integrated server）或客户端实体未挂载该 power 时，phc.getPower 可能返回 null。
                    // 防御：跳过而非 NPE 刷屏（等下次真实挂载后再同步）。
                    Apoli.LOGGER.warn("Received sync packet for power type not held by entity: " + powerId);
                    return;
                }
                if (powerNbt == null) {
                    Apoli.LOGGER.warn("Received sync packet with null power data: " + powerId);
                    return;
                }
                power.fromTag(powerNbt, context.client().level.registryAccess());
            }, () -> Apoli.LOGGER.warn("Received sync packet for entity without power holder."));
        });
    }
}

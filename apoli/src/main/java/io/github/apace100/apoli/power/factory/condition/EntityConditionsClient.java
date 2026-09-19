package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.mixin.ClientAdvancementManagerAccessor;
import io.github.apace100.apoli.mixin.ClientPlayerInteractionManagerAccessor;
import io.github.apace100.apoli.mixin.ServerPlayerInteractionManagerAccessor;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public final class EntityConditionsClient {

    @SuppressWarnings("unchecked")
    @Environment(EnvType.CLIENT)
    public static void register() {
        register(new ConditionFactory<>(Apoli.identifier("using_effective_tool"), new SerializableData(),
            (data, entity) -> {
                if(entity instanceof ServerPlayer) {
                    ServerPlayerInteractionManagerAccessor interactionMngr = ((ServerPlayerInteractionManagerAccessor)((ServerPlayer)entity).gameMode);
                    if(interactionMngr.getIsDestroyingBlock()) {
                        return ((Player)entity).hasCorrectToolForDrops(entity.level().getBlockState(interactionMngr.getDestroyPos()));
                    }
                } else
                if(entity instanceof LocalPlayer) {
                    ClientPlayerInteractionManagerAccessor interactionMngr = (ClientPlayerInteractionManagerAccessor) Minecraft.getInstance().gameMode;
                    if(interactionMngr.getIsDestroying()) {
                        return ((Player)entity).hasCorrectToolForDrops(entity.level().getBlockState(interactionMngr.getDestroyBlockPos()));
                    }
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("gamemode"), new SerializableData()
            .add("gamemode", SerializableDataTypes.STRING), (data, entity) -> {
            if(entity instanceof ServerPlayer) {
                ServerPlayerInteractionManagerAccessor interactionMngr = ((ServerPlayerInteractionManagerAccessor)((ServerPlayer)entity).gameMode);
                return interactionMngr.getGameModeForPlayer().getName().equals(data.getString("gamemode"));
            } else
            if(entity instanceof LocalPlayer) {
                ClientPlayerInteractionManagerAccessor interactionMngr = (ClientPlayerInteractionManagerAccessor) Minecraft.getInstance().gameMode;
                return interactionMngr.getLocalPlayerMode().getName().equals(data.getString("gamemode"));
            }
            return false;
        }));
        register(new ConditionFactory<>(Apoli.identifier("advancement"), new SerializableData()
            .add("advancement", SerializableDataTypes.IDENTIFIER), (data, entity) -> {
            ResourceLocation id = data.getId("advancement");
            if(entity instanceof ServerPlayer) {
                AdvancementHolder advancement = entity.getServer().getAdvancements().get(id);
                if(advancement == null) {
                    Apoli.LOGGER.warn("Advancement \"" + id + "\" did not exist, but was referenced in an \"origins:advancement\" condition.");
                } else {
                    return ((ServerPlayer)entity).getAdvancements().getOrStartProgress(advancement).isDone();
                }
            } else
            if(entity instanceof LocalPlayer) {
                ClientAdvancements advancementManager = Minecraft.getInstance().getConnection().getAdvancements();
                AdvancementHolder advancement = advancementManager.get(id);
                if(advancement != null) {
                    Map<Advancement, AdvancementProgress> progressMap = ((ClientAdvancementManagerAccessor)advancementManager).getProgress();
                    if(progressMap.containsKey(advancement.value())) {
                        return progressMap.get(advancement.value()).isDone();
                    }
                }
                // We don't want to print an error here if the advancement does not exist,
                // because on the client-side the advancement could just not have been received from the server.
            }
            return false;
        }));
        register(new ConditionFactory<>(Apoli.identifier("glowing"), new SerializableData(),
            (data, entity) -> Minecraft.getInstance().shouldEntityAppearGlowing(entity)));
    }

    private static void register(ConditionFactory<Entity> conditionFactory) {
        Registry.register(ApoliRegistries.ENTITY_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}

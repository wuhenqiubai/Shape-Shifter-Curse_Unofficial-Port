package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.mixin.ServerPlayerInteractionManagerAccessor;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class EntityConditionsServer {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ConditionFactory<>(Apoli.identifier("using_effective_tool"), new SerializableData(),
            (data, entity) -> {
                if(entity instanceof ServerPlayer) {
                    ServerPlayerInteractionManagerAccessor interactionMngr = ((ServerPlayerInteractionManagerAccessor)((ServerPlayer)entity).gameMode);
                    if(interactionMngr.getIsDestroyingBlock()) {
                        return ((Player)entity).hasCorrectToolForDrops(entity.level().getBlockState(interactionMngr.getDestroyPos()));
                    }
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("gamemode"), new SerializableData()
            .add("gamemode", SerializableDataTypes.STRING), (data, entity) -> {
            if(entity instanceof ServerPlayer) {
                ServerPlayerInteractionManagerAccessor interactionMngr = ((ServerPlayerInteractionManagerAccessor)((ServerPlayer)entity).gameMode);
                return interactionMngr.getGameModeForPlayer().getName().equals(data.getString("gamemode"));
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
            }
            return false;
        }));
        register(new ConditionFactory<>(Apoli.identifier("glowing"), new SerializableData(),
            (data, entity) -> entity.isCurrentlyGlowing()));
    }

    private static void register(ConditionFactory<Entity> conditionFactory) {
        Registry.register(ApoliRegistries.ENTITY_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}

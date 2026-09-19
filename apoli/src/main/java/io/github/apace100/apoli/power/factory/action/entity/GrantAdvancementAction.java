package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class GrantAdvancementAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if (entity instanceof ServerPlayer player) {
            ResourceLocation id = data.getId("advancement");
            if (player.getServer() != null) {
                AdvancementHolder adv = player.getServer().getAdvancements().get(id);
                grant(player, adv);
            }
        }
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apoli.identifier("grant_advancement"),
                new SerializableData()
                        .add("advancement", SerializableDataTypes.IDENTIFIER),
                GrantAdvancementAction::action
        );
    }

    private static void grant(ServerPlayer player, AdvancementHolder advancement) {
        AdvancementProgress advancementProgress = player.getAdvancements().getOrStartProgress(advancement);
        if (!advancementProgress.isDone()) {
            for (String criterion : advancementProgress.getRemainingCriteria()) {
                player.getAdvancements().award(advancement, criterion);
            }
        }
    }
}

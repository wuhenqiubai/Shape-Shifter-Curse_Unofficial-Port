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

public class RevokeAdvancementAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if (entity instanceof ServerPlayer player) {
            ResourceLocation id = data.getId("advancement");
            if (player.getServer() != null) {
                AdvancementHolder adv = player.getServer().getAdvancements().get(id);
                revoke(player, adv);
            }
        }
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apoli.identifier("revoke_advancement"),
                new SerializableData()
                        .add("advancement", SerializableDataTypes.IDENTIFIER),
                RevokeAdvancementAction::action
        );
    }

    private static void revoke(ServerPlayer player, AdvancementHolder advancement) {
        AdvancementProgress advancementProgress = player.getAdvancements().getOrStartProgress(advancement);
        if (advancementProgress.hasProgress()) {
            for (String string : advancementProgress.getCompletedCriteria()) {
                player.getAdvancements().revoke(advancement, string);
            }
        }
    }
}

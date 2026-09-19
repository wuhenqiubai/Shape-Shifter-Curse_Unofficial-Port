package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.world.entity.Entity;

public class ModifyStatAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof ServerPlayer serverPlayerEntity)) return;

        Stat<?> stat = data.get("stat");
        ServerStatsCounter serverStatHandler = serverPlayerEntity.getStats();

        int newValue;
        int originalValue = serverStatHandler.getValue(stat);

        serverPlayerEntity.resetStat(stat);

        Modifier modifier = data.get("modifier");
        newValue = (int)modifier.apply(entity, originalValue);

        serverPlayerEntity.awardStat(stat, newValue);
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apoli.identifier("modify_stat"),
            new SerializableData()
                .add("stat", SerializableDataTypes.STAT)
                .add("modifier", Modifier.DATA_TYPE),
            ModifyStatAction::action
        );
    }
}

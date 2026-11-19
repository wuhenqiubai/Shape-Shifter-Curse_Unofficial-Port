package io.github.apace100.apoli.power.factory.action.bientity;

import com.mojang.datafixers.util.Pair;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

public class DamageAction {

    public static void action(SerializableData.Instance data, Pair<Entity, Entity> entities) {
        float amount = data.get("amount");
        DamageSource source = MiscUtil.createDamageSource(
                entities.getFirst().damageSources(), data.get("source"), data.get("damage_type"), entities.getFirst());
        entities.getSecond().hurt(source, amount);
    }

    public static ActionFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionFactory<>(Apoli.identifier("damage"),
            new SerializableData()
                .add("amount", SerializableDataTypes.FLOAT)
                .add("source", ApoliDataTypes.DAMAGE_SOURCE_DESCRIPTION, null)
                .add("damage_type", SerializableDataTypes.DAMAGE_TYPE, null),
            DamageAction::action
        );
    }
}

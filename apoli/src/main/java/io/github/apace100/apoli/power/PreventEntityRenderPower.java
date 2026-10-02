package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Predicate;

public class PreventEntityRenderPower extends Power {

    private final Predicate<Entity> entityCondition;
    private final Predicate<Tuple<Entity, Entity>> bientityCondition;

    public PreventEntityRenderPower(PowerType<?> type, LivingEntity entity, Predicate<Entity> entityCondition, Predicate<Tuple<Entity, Entity>> bientityCondition) {
        super(type, entity);
        this.entityCondition = entityCondition;
        this.bientityCondition = bientityCondition;
    }

    public boolean doesApply(Entity e) {
        return (entityCondition == null || entityCondition.test(e))
            && (bientityCondition == null || bientityCondition.test(new Tuple<>(entity, e)));
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("prevent_entity_render"),
            new SerializableData()
                .add("entity_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
            data ->
                (type, player) -> new PreventEntityRenderPower(type, player,
                    (ConditionFactory<Entity>.Instance)data.get("entity_condition"),
                    (Predicate<Tuple<Entity, Entity>>)data.get("bientity_condition")))
            .allowCondition();
    }
}

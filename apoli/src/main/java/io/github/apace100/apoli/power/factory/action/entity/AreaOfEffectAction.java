package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class AreaOfEffectAction {
    public static void action(SerializableData.Instance data, Entity entity) {
        Consumer<Tuple<Entity, Entity>> bientityAction = data.get("bientity_action");
        Predicate<Tuple<Entity, Entity>> bientityCondition = data.get("bientity_condition");
        boolean includeTarget = data.get("include_target");
        double radius = data.get("radius");
        double diameter = radius * 2;

        for (Entity check : entity.level().getEntitiesOfClass(Entity.class, AABB.ofSize(entity.getPosition(1F), diameter, diameter, diameter))) {
            if (check == entity && !includeTarget)
                continue;
            Tuple<Entity, Entity> actorTargetPair = new Tuple<>(entity, check);
            if ((bientityCondition == null || bientityCondition.test(actorTargetPair)) && check.distanceToSqr(entity) < radius * radius)
                bientityAction.accept(actorTargetPair);
        }
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apoli.identifier("area_of_effect"),
            new SerializableData()
                .add("radius", SerializableDataTypes.DOUBLE, 16D)
                .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("include_target", SerializableDataTypes.BOOLEAN, false),
            AreaOfEffectAction::action
        );
    }
}


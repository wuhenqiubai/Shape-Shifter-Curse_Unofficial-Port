package io.github.apace100.apoli.power.legacy;

import com.mojang.datafixers.util.Pair;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Predicate;

public class ModifyBehaviorPower extends Power {
    private final Predicate<Pair<Entity, Entity>> biEntityCondition;
    private final BehaviorType behaviorType;

    public ModifyBehaviorPower(PowerType<?> type, LivingEntity entity, Predicate<Pair<Entity, Entity>> biEntityCondition, BehaviorType behaviorType) {
        super(type, entity);
        this.biEntityCondition = biEntityCondition;
        this.behaviorType = behaviorType;
    }

    public BehaviorType getBehaviorType() {
        return behaviorType;
    }

    public boolean doesApply(Entity e) {
        return biEntityCondition.test(new Pair<>(this.entity, e));
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.legacy("modify_behavior"),
            new SerializableData()
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION)
                .add("behavior_type", SerializableDataType.enumValue(BehaviorType.class)),
            data -> (type, player) ->
                new ModifyBehaviorPower(type, player, data.get("bientity_condition"), data.get("behavior_type"))
        )
            .allowCondition();
    }

    public enum BehaviorType implements StringRepresentable {
        PASSIVE("passive"),
        NEUTRAL("neutral"),
        HOSTILE("hostile");

        private final String serialized;

        BehaviorType(String serialized) {
            this.serialized = serialized;
        }

        @Override
        public String getSerializedName() {
            return this.serialized;
        }
    }
}

package io.github.apace100.apoli.power;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class SetEntityGroupPower extends Power {

    public final List<TagKey<EntityType<?>>> groupTags;

    public SetEntityGroupPower(PowerType<?> type, LivingEntity entity, List<TagKey<EntityType<?>>> groupTags) {
        super(type, entity);
        this.groupTags = groupTags;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("entity_group"),
            new SerializableData()
                .add("group", SerializableDataTypes.ENTITY_GROUP, List.of())
                .add("group_tags", SerializableDataType.list(SerializableDataType.tag(Registries.ENTITY_TYPE)), List.of()),
            data ->
                (type, player) -> new SetEntityGroupPower(type, player, Lists.newArrayList(Iterables.concat(data.get("group"), data.get("group_tags")))))
            .allowCondition();
    }
}

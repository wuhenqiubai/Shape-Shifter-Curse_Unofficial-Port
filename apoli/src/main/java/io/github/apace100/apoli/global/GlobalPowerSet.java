package io.github.apace100.apoli.global;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeReference;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObject;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.util.TagLike;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class GlobalPowerSet implements Comparable<GlobalPowerSet>, DataObject<GlobalPowerSet> {
    public static final Codec<GlobalPowerSet> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                Codec.INT
                    .optionalFieldOf("order", 0)
                    .forGetter(GlobalPowerSet::getOrder),
                TagLike.codec(BuiltInRegistries.ENTITY_TYPE)
                    .optionalFieldOf("entity_types", null)
                    .forGetter(set -> set.entityTypes),
                PowerTypeReference.CODEC.listOf()
                    .fieldOf("powers")
                    .forGetter(GlobalPowerSet::getPowerTypes)
            )
            .apply(instance, GlobalPowerSet::new)
    );

    private final int order;
    private final TagLike<EntityType<?>> entityTypes;

    public List<PowerType<?>> getPowerTypes() {
        return powerTypes;
    }

    private final List<PowerType<?>> powerTypes;

    public GlobalPowerSet(int order, TagLike<EntityType<?>> entityTypes, List<PowerType<?>> powerTypes) {
        this.order = order;
        this.entityTypes = entityTypes;
        this.powerTypes = powerTypes;
    }

    public boolean doesApply(EntityType<?> entityType) {
        return entityTypes == null || entityTypes.contains(entityType);
    }

    public boolean doesApply(Entity entity) {
        return doesApply(entity.getType());
    }

    public int getOrder() {
        return order;
    }

    @Override
    public int compareTo(@NotNull GlobalPowerSet o) {
        return Integer.compare(order, o.order);
    }

    /**
     * Checks the defined power type references of this set to validate
     * that they all exist and have been loaded correctly.
     * Invalid power types will be removed from this set as a side effect.
     * @return List containing all invalid power types that were removed from the set
     */
    public List<PowerType<?>> validate() {
        List<PowerType<?>> invalid = powerTypes.stream().filter(pt -> !PowerTypeRegistry.contains(pt.getIdentifier())).collect(Collectors.toList());
        powerTypes.removeAll(invalid);
        invalid.removeIf(pt -> PowerTypeRegistry.isDisabled(pt.getIdentifier()));
        return invalid;
    }

    @Override
    public DataObjectFactory<GlobalPowerSet> getFactory() {
        return FACTORY;
    }

    public static final SerializableData DATA = new SerializableData()
            .add("entity_types", SerializableDataTypes.ENTITY_TYPE_TAG_LIKE, null)
            .add("powers", SerializableDataType.list(ApoliDataTypes.POWER_TYPE))
            .add("order", SerializableDataTypes.INT, 0);

    public static final DataObjectFactory<GlobalPowerSet> FACTORY = new Factory();

    private static class Factory implements DataObjectFactory<GlobalPowerSet> {

        @Override
        public SerializableData getData() {
            return DATA;
        }

        @Override
        public GlobalPowerSet fromData(SerializableData.Instance instance) {
            return new GlobalPowerSet(instance.getInt("order"), instance.get("entity_types"), instance.get("powers"));
        }

        @Override
        public SerializableData.Instance toData(GlobalPowerSet globalPowerSet) {
            SerializableData.Instance inst = DATA.new Instance();
            inst.set("order", globalPowerSet.order);
            inst.set("entity_types", globalPowerSet.entityTypes);
            inst.set("powers", globalPowerSet.powerTypes);
            return inst;
        }
    }
}

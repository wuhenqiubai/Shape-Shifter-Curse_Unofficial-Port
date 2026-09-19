package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.BiomeWeatherAccess;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

import java.util.List;

public class BiomeConditions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ConditionFactory<>(Apoli.identifier("constant"), new SerializableData()
            .add("value", SerializableDataTypes.BOOLEAN),
            (data, biome) -> data.getBoolean("value")));
        register(new ConditionFactory<>(Apoli.identifier("and"), new SerializableData()
            .add("conditions", ApoliDataTypes.BIOME_CONDITIONS),
            (data, biome) -> ((List<ConditionFactory<Holder<Biome>>.Instance>)data.get("conditions")).stream().allMatch(
                condition -> condition.test(biome)
            )));
        register(new ConditionFactory<>(Apoli.identifier("or"), new SerializableData()
            .add("conditions", ApoliDataTypes.BIOME_CONDITIONS),
            (data, biome) -> ((List<ConditionFactory<Holder<Biome>>.Instance>)data.get("conditions")).stream().anyMatch(
                condition -> condition.test(biome)
            )));

        register(new ConditionFactory<>(Apoli.identifier("high_humidity"), new SerializableData(),
            (data, biome) -> ((BiomeWeatherAccess)(Object)biome.value()).getDownfall() > 0.85f));
        register(new ConditionFactory<>(Apoli.identifier("temperature"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
            (data, biome) -> ((Comparison)data.get("comparison")).compare(biome.value().getBaseTemperature(), data.getFloat("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("category"), new SerializableData() // Deprecated
            .add("category", SerializableDataTypes.STRING),
            (data, biome) -> {
                ResourceLocation tagId = Apoli.identifier("category/" + data.getString("category"));
                TagKey<Biome> biomeTag = TagKey.create(Registries.BIOME, tagId);
                return biome.is(biomeTag);
            }));
        register(new ConditionFactory<>(Apoli.identifier("precipitation"), new SerializableData()
            .add("precipitation", SerializableDataType.enumValue(Biome.Precipitation.class)),
            (data, biome) -> biome.value().getPrecipitationAt(new BlockPos(0, 64, 0)).equals(data.get("precipitation"))));
        register(new ConditionFactory<>(Apoli.identifier("in_tag"), new SerializableData()
            .add("tag", SerializableDataTypes.BIOME_TAG),
            (data, biome) -> {
                TagKey<Biome> biomeTag = data.get("tag");
                return biome.is(biomeTag);
            }));
    }

    private static void register(ConditionFactory<Holder<Biome>> conditionFactory) {
        Registry.register(ApoliRegistries.BIOME_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}

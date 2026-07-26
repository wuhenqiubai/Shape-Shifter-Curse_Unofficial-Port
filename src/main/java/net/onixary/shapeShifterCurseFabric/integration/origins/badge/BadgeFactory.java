package net.onixary.shapeShifterCurseFabric.integration.origins.badge;

import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.registry.DataObjectFactory;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public record BadgeFactory(ResourceLocation id, SerializableData data, Function<SerializableData.Instance, Badge> factory) implements DataObjectFactory<Badge> {

    @Override
    public SerializableData getData() {
        return data;
    }

    @Override
    public Badge fromData(SerializableData.Instance instance) {
        return factory.apply(instance);
    }

    @Override
    public SerializableData.Instance toData(Badge badge) {
        return badge.toData(data.new Instance());
    }

}
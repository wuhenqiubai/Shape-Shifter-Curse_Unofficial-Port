package io.github.apace100.apoli.power.factory;

import io.github.apace100.calio.data.SerializableData;
import net.minecraft.resources.ResourceLocation;

public interface Factory {

    ResourceLocation getSerializerId();

    SerializableData getSerializableData();

}

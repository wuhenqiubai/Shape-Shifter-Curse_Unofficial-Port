package io.github.apace100.calio.registry;

import io.github.apace100.calio.data.SerializableData;
import net.minecraft.resources.ResourceLocation;

public record DataObjectPair(ResourceLocation factory, SerializableData.Instance data) {
}

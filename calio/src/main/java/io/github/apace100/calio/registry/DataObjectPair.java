package io.github.apace100.calio.registry;

import io.github.apace100.calio.data.SerializableData;
import net.minecraft.resources.Identifier;

public record DataObjectPair(Identifier factory, SerializableData.Instance data) {
}

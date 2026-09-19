package io.github.apace100.apoli.access;

import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceLocation;

public interface IdentifiedLootTable {

    void apoli$setId(ResourceLocation id, HolderGetter.Provider lootManager);

    ResourceLocation apoli$getId();
}

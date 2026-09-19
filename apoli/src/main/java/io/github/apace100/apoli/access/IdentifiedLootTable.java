package io.github.apace100.apoli.access;

import net.minecraft.core.HolderGetter;
import net.minecraft.resources.Identifier;

public interface IdentifiedLootTable {

    void setId(Identifier id, HolderGetter.Provider lootManager);

    Identifier getId();
}

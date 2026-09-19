package io.github.apace100.apoli.access;

import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public interface ReplacingLootContextParameterSet {

    void setType(LootContextParamSet type);

    LootContextParamSet getType();

}

package io.github.apace100.apoli.access;

import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public interface ReplacingLootContext {

    void setType(LootContextParamSet type);

    LootContextParamSet getType();

    void setReplaced(LootTable table);

    boolean isReplaced(LootTable table);
}

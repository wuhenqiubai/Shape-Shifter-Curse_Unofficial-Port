package io.github.apace100.apoli.access;

import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.LootTable;

public interface ReplacingLootContext {

    void setType(ContextKeySet type);

    ContextKeySet getType();

    void setReplaced(LootTable table);

    boolean isReplaced(LootTable table);
}

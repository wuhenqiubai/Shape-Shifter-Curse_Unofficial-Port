package io.github.apace100.apoli.access;

import net.minecraft.util.context.ContextKeySet;

public interface ReplacingLootContextParameterSet {

    void setType(ContextKeySet type);

    ContextKeySet getType();

}

package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.ReplacingLootContext;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashSet;
import java.util.Set;

@Mixin(LootContext.class)
public class LootContextMixin implements ReplacingLootContext {

    @Unique
    private ContextKeySet apoli$lootContextType;

    @Unique
    private final Set<LootTable> apoli$replacedTables = new HashSet<>();

    @Override
    public void setType(ContextKeySet type) {
        apoli$lootContextType = type;
    }

    @Override
    public ContextKeySet getType() {
        return apoli$lootContextType;
    }

    @Override
    public void setReplaced(LootTable table) {
        apoli$replacedTables.add(table);
    }

    @Override
    public boolean isReplaced(LootTable table) {
        return apoli$replacedTables.contains(table);
    }
}

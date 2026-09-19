package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.ReplacingLootContext;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashSet;
import java.util.Set;

@Mixin(LootContext.class)
public class LootContextMixin implements ReplacingLootContext {

    @Unique
    private LootContextParamSet apoli$lootContextType;

    @Unique
    private final Set<LootTable> apoli$replacedTables = new HashSet<>();

    @Override
    public void setType(LootContextParamSet type) {
        apoli$lootContextType = type;
    }

    @Override
    public LootContextParamSet getType() {
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

package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.ReplacingLootContextParameterSet;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.LootParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LootParams.class)
public class LootContextParameterSetMixin implements ReplacingLootContextParameterSet {

    @Unique
    private ContextKeySet apoli$lootContextType;

    @Override
    public void setType(ContextKeySet type) {
        apoli$lootContextType = type;
    }

    @Override
    public ContextKeySet getType() {
        return apoli$lootContextType;
    }

}

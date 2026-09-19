package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.ReplacingLootContextParameterSet;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LootParams.class)
public class LootContextParameterSetMixin implements ReplacingLootContextParameterSet {

    @Unique
    private LootContextParamSet apoli$lootContextType;

    @Override
    public void setType(LootContextParamSet type) {
        apoli$lootContextType = type;
    }

    @Override
    public LootContextParamSet getType() {
        return apoli$lootContextType;
    }

}

package io.github.apace100.apoli.data;

import io.github.apace100.apoli.Apoli;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class LegacyMaterial {
    private final TagKey<Block> materialTagKey;
    private final String material;

    public LegacyMaterial(String material) {
        materialTagKey = TagKey.create(Registries.BLOCK, Apoli.identifier("material/" + material));
        this.material = material;
    }
    public String getMaterial() {
        return this.material;
    }

    public boolean blockStateIsOfMaterial(BlockState blockState) {
        return blockState.is(materialTagKey);
    }
}

package io.github.apace100.apoli.power;

import com.google.common.collect.ImmutableList;
import io.github.apace100.apoli.power.factory.PowerFactory;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class MultiplePowerType<T extends Power> extends PowerType<T> {

    private ImmutableList<ResourceLocation> subPowers;

    public MultiplePowerType(ResourceLocation id, PowerFactory<T>.Instance factory) {
        super(id, factory);
    }

    public void setSubPowers(List<ResourceLocation> subPowers) {
        this.subPowers = ImmutableList.copyOf(subPowers);
    }

    public ImmutableList<ResourceLocation> getSubPowers() {
        return subPowers;
    }
}

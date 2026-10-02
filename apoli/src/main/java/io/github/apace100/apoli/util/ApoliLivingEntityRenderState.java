package io.github.apace100.apoli.util;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.world.item.equipment.Equippable;

public interface ApoliLivingEntityRenderState {
    void apoli$setPowerHolder(PowerHolderComponent component);
    PowerHolderComponent apoli$getPowerHolder();

    void apoli$setCachedEquippable(Equippable equippable);
    Equippable apoli$getCachedEquippable();
}

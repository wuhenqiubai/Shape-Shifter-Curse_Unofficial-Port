package io.github.apace100.apoli.legacy;

import net.minecraft.world.entity.LivingEntity;

public interface OverlayableComponentsItemStack {
    default void apoli_legacy$updateOverlayableComponents(LivingEntity entity) {
        throw new IllegalStateException();
    }
}

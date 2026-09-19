package io.github.apace100.apoli.access;

import net.minecraft.world.entity.Entity;

public interface EntityLinkedItemStack {
    Entity getEntity();

    void setEntity(Entity entity);
}

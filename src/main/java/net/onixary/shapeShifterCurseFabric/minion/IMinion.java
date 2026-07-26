package net.onixary.shapeShifterCurseFabric.minion;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public interface IMinion<T extends LivingEntity> {
    void InitMinion(Player player);

    void setOwner(Player player);

    UUID getMinionOwnerUUID();

    void setMinionOwnerUUID(UUID uuid);

    ResourceLocation getMinionTypeID();

    T getSelf();
}
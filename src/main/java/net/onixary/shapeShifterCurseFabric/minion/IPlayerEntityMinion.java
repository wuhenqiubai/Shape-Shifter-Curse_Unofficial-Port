package net.onixary.shapeShifterCurseFabric.minion;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public interface IPlayerEntityMinion {
    public ConcurrentHashMap<ResourceLocation, ArrayList<UUID>> shape_shifter_curse$getAllMinions();

    public ArrayList<UUID> shape_shifter_curse$getMinionsByMinionID(ResourceLocation MinionID);

    public int shape_shifter_curse$getMinionsCount();

    public int shape_shifter_curse$getMinionsCount(ResourceLocation MinionID);

    public boolean shape_shifter_curse$minionExist(ResourceLocation MinionID, UUID minionUUID);

    public boolean shape_shifter_curse$removeMinion(ResourceLocation MinionID, UUID minionUUID);

    public <T extends IMinion<? extends LivingEntity>> boolean shape_shifter_curse$addMinion(T minion);

    public void shape_shifter_curse$applyCooldown(ResourceLocation MinionID, long time);

    public long shape_shifter_curse$getCooldownTime(ResourceLocation MinionID);

    public void shape_shifter_curse$resetAllCooldown();

    public void shape_shifter_curse$clearAllMinions();

    public void shape_shifter_curse$clearMinions(ResourceLocation MinionID);
}
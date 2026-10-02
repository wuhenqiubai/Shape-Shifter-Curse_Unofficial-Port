package io.github.apace100.apoli.access;

import io.github.apace100.apoli.power.ModifyGrindstonePower;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Optional;

public interface PowerModifiedGrindstone {

    List<ModifyGrindstonePower> getAppliedPowers();

    Player getPlayer();

    Optional<BlockPos> getPos();
}

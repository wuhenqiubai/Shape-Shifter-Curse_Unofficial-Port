package io.github.apace100.apoli.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultiPlayerGameMode.class)
public interface ClientPlayerInteractionManagerAccessor {

    @Accessor
    BlockPos getDestroyBlockPos();

    @Accessor
    boolean getIsDestroying();

    @Accessor
    GameType getLocalPlayerMode();
}

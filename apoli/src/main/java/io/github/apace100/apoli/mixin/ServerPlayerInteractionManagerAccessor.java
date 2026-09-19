package io.github.apace100.apoli.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayerGameMode.class)
public interface ServerPlayerInteractionManagerAccessor {

    @Accessor
    BlockPos getDestroyPos();

    @Accessor
    boolean getIsDestroyingBlock();

    @Accessor
    GameType getGameModeForPlayer();
}

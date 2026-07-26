package net.onixary.shapeShifterCurseFabric.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.jetbrains.annotations.Nullable;

public class CachedBlockPositionData extends BlockInWorld {
    private final BlockState stateCache;
    private final BlockEntity blockEntityCache;

    public CachedBlockPositionData(LevelReader world, BlockPos pos, boolean forceLoad, BlockState state, @Nullable BlockEntity blockEntity) {
        super(world, pos, forceLoad);
        this.stateCache = state;
        this.blockEntityCache = blockEntity;
    }

    public BlockState getState() {
        return this.stateCache;
    }

    @Nullable
    public BlockEntity getEntity() {
        return this.blockEntityCache;
    }
}

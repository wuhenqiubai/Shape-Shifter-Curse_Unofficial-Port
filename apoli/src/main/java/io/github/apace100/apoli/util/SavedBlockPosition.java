package io.github.apace100.apoli.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public class SavedBlockPosition extends BlockInWorld {

    private final BlockState blockState;
    private final BlockEntity blockEntity;

    public SavedBlockPosition(LevelReader world, BlockPos pos) {
        super(world, pos, true);
        this.blockState = world.getBlockState(pos);
        this.blockEntity = world.getBlockEntity(pos);
    }

    @Override
    public BlockState getState() {
        return blockState;
    }

    @Override
    public BlockEntity getEntity() {
        return blockEntity;
    }

    @Override
    public LevelReader getLevel() {
        return super.getLevel();
    }

    @Override
    public BlockPos getPos() {
        return super.getPos();
    }
}

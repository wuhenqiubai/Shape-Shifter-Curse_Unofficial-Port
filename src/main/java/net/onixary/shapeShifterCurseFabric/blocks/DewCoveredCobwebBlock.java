package net.onixary.shapeShifterCurseFabric.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;

public class DewCoveredCobwebBlock extends Block {
    public DewCoveredCobwebBlock(Properties settings) {
        super(settings);
    }

    @Override
    public @NonNull VoxelShape getCollisionShape(@NonNull BlockState state, @NonNull BlockGetter world, @NonNull BlockPos pos, @NonNull CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public float getSpeedFactor() {
        return 0.25f;
    }

    @Override
    public void entityInside(@NonNull BlockState state, @NonNull Level level, @NonNull BlockPos pos, Entity entity, @NonNull InsideBlockEffectApplier insideBlockEffectApplier, boolean bl) {
        entity.makeStuckInBlock(state, new Vec3(0.25, 0.05, 0.25));
    }
}
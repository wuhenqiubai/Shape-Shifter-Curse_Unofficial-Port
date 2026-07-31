package net.onixary.shapeShifterCurseFabric.integration.origins.content;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;

public class TemporaryCobwebBlock extends WebBlock {

	public TemporaryCobwebBlock(BlockBehaviour.Properties settings) {
		super(settings);
	}

	@Override
	public void tick(@NonNull BlockState state, ServerLevel world, @NonNull BlockPos pos, @NonNull RandomSource random) {
		if(!world.isClientSide()) {
			world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
		}
	}

	@Override
	public @NonNull VoxelShape getShape(@NonNull BlockState state, @NonNull BlockGetter world, @NonNull BlockPos pos, @NonNull CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	public @NonNull VoxelShape getCollisionShape(@NonNull BlockState state, @NonNull BlockGetter world, @NonNull BlockPos pos, @NonNull CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	public void onPlace(@NonNull BlockState state, Level worldIn, @NonNull BlockPos pos, @NonNull BlockState oldState, boolean isMoving) {
		worldIn.scheduleTick(pos, this, 60);
		super.onPlace(state, worldIn, pos, oldState, isMoving);
	}

	@Override
	public void entityInside(@NonNull BlockState state, @NonNull Level level, @NonNull BlockPos pos, @NonNull Entity entity, @NonNull InsideBlockEffectApplier insideBlockEffectApplier, boolean bl) {
		//if (PowerTypes.WEBBING.isActive(entityIn)) {
		//	return;
		//}
		super.entityInside(state, level, pos, entity, insideBlockEffectApplier, bl);
	}
}
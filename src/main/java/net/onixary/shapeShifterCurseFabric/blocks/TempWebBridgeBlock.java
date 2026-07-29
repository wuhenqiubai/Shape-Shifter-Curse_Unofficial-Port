package net.onixary.shapeShifterCurseFabric.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TempWebBridgeBlock extends HorizontalDirectionalBlock {
    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    public static final Property<Direction> HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape voxelShape = Block.box(0.0F, 14.0F, 0.0F, 16.0F, 16.0F, 16.0F);
    private static final VoxelShape voxelShape2 = Block.box(0.0F, 0.0F, 0.0F, 2.0F, 16.0F, 2.0F);
    private static final VoxelShape voxelShape3 = Block.box(14.0F, 0.0F, 0.0F, 16.0F, 16.0F, 2.0F);
    private static final VoxelShape voxelShape4 = Block.box(0.0F, 0.0F, 14.0F, 2.0F, 16.0F, 16.0F);
    private static final VoxelShape voxelShape5 = Block.box(14.0F, 0.0F, 14.0F, 16.0F, 16.0F, 16.0F);
    private static final VoxelShape NORMAL_OUTLINE_SHAPE = Shapes.or(voxelShape, voxelShape2, voxelShape3, voxelShape4, voxelShape5);

    public TempWebBridgeBlock(BlockBehaviour.Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0).setValue(HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return simpleCodec(TempWebBridgeBlock::new);
    }

    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        this.tick(state, world, pos, random);
    }

    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        // if ((random.nextInt(3) == 0 || this.canIncreaseAge(world, pos, 4)) && this.increaseAge(state, world, pos)) {
        //     BlockPos.Mutable mutable = new BlockPos.Mutable();
        //     for(Direction direction : Direction.values()) {
        //         mutable.set(pos, direction);
        //         BlockState blockState = world.getBlockState(mutable);
        //         if (blockState.isOf(this) && !this.increaseAge(blockState, world, mutable)) {
        //             world.scheduleBlockTick(mutable, this, MathHelper.nextInt(random, 20, 40));
        //         }
        //     }
        // } else {
        //     world.scheduleBlockTick(pos, this, MathHelper.nextInt(random, 20, 40));
        // }
        boolean BlockRemoved = false;
        if (this.canIncreaseAge(world, pos, 3) && random.nextInt(12) == 0) {
            BlockRemoved = this.increaseAge(state, world, pos);
        } else if (this.canIncreaseAge(world, pos, 2) && random.nextInt(6) == 0) {
            BlockRemoved = this.increaseAge(state, world, pos);
        } else if (random.nextInt(3) == 0) {
            BlockRemoved = this.increaseAge(state, world, pos);
        }
        if (!BlockRemoved) {
            world.scheduleTick(pos, this, Mth.nextInt(random, 150, 300));  // 7.5s~15s
        }
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()) {
            mutable.setWithOffset(pos, direction);
            BlockState blockState = world.getBlockState(mutable);
            if (blockState.is(this)) {
                world.scheduleTick(mutable, this, Mth.nextInt(random, 150, 300));
            }
        }
    }

    private boolean increaseAge(BlockState state, Level world, BlockPos pos) {
        int i = state.getValue(AGE);
        if (i < 3) {
            world.setBlock(pos, state.setValue(AGE, i + 1), 2);
            return false;
        } else {
            world.removeBlock(pos, false);
            return true;
        }
    }


    // 虽然这样做可以让破碎更美观 但是会严重加速垂直的破碎速度
    // public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
    //     if (sourceBlock.getDefaultState().isOf(this) && this.canIncreaseAge(world, pos, 2)) {
    //         world.removeBlock(pos, false);
    //     }
    //     super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
    // }

    private boolean canIncreaseAge(BlockGetter world, BlockPos pos, int maxNeighbors) {
        int i = 0;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for(Direction direction : Direction.values()) {
            mutable.setWithOffset(pos, direction);
            if (world.getBlockState(mutable).is(this)) {
                ++i;
                if (i >= maxNeighbors) {
                    return false;
                }
            }
        }

        return true;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, HORIZONTAL_FACING);
    }

    public ItemStack getPickStack(BlockGetter world, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (context.isAbove(Shapes.block(), pos, true) && !context.isDescending()) {
            return NORMAL_OUTLINE_SHAPE;
        } else {
            return Shapes.empty();
        }
    }

    public VoxelShape getInteractionShape(BlockState state, BlockGetter world, BlockPos pos) {
        return Shapes.block();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state) {
        return true;
    }
}
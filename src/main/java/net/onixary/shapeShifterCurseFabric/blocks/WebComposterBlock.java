package net.onixary.shapeShifterCurseFabric.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.onixary.shapeShifterCurseFabric.items.RegCustomItem;
import net.onixary.shapeShifterCurseFabric.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class WebComposterBlock extends Block implements WorldlyContainerHolder {
    public static final int MIN_LEVEL = 0;
    public static final int MAX_LEVEL = 3;
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 4);
    private static final VoxelShape RAYCAST_SHAPE = Shapes.block();
    private static final VoxelShape[] LEVEL_TO_COLLISION_SHAPE = Util.make(new VoxelShape[5], (shapes) -> {
        for(int i = 0; i < 4; ++i) {
            shapes[i] = Shapes.join(RAYCAST_SHAPE, Block.box(2.0F, Math.max(2, 1 + i * 4), 2.0F, 14.0F, 16.0F, 14.0F), BooleanOp.ONLY_FIRST);
        }

        shapes[4] = shapes[3];
    });

	public static final IntegerProperty COCOON_COUNT = IntegerProperty.create("cocoons_count", 0, 64);
    public static Item ResultItem = RegCustomItem.SPIDER_FLUID_COCOON;
    public static Function<RandomSource, Integer> ResultCount = (random) -> 4 + random.nextInt(3);

    public static boolean canIncrease(ItemStack itemStack) {
        if (itemStack.is(ModTags.Meat_Tag)) {
            return true;
        }
        FoodProperties foodComponent = itemStack.get(DataComponents.FOOD);
	    return foodComponent != null;
    }

    public static float getIncreaseChance(ItemStack itemStack) {
        FoodProperties foodComponent = itemStack.get(DataComponents.FOOD);
        if (foodComponent != null) {
            return 0.55f;
        }
        return 0.5f;
    }

    public WebComposterBlock(Properties settings) {
        super(settings);
	    this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, MIN_LEVEL).setValue(COCOON_COUNT, 0));
    }

    public static void playEffects(Level world, BlockPos pos, boolean fill) {
        BlockState blockState = world.getBlockState(pos);
        world.playLocalSound(pos, fill ? SoundEvents.COMPOSTER_FILL_SUCCESS : SoundEvents.COMPOSTER_FILL, SoundSource.BLOCKS, 1.0F, 1.0F, false);
        double d = blockState.getShape(world, pos).max(Direction.Axis.Y, 0.5F, 0.5F) + (double)0.03125F;
        double e = 0.13125F;
        double f = 0.7375F;
        RandomSource random = world.getRandom();

        for(int i = 0; i < 10; ++i) {
            double g = random.nextGaussian() * 0.02;
            double h = random.nextGaussian() * 0.02;
            double j = random.nextGaussian() * 0.02;
            world.addParticle(ParticleTypes.COMPOSTER, (double)pos.getX() + (double)0.13125F + (double)0.7375F * (double)random.nextFloat(), (double)pos.getY() + d + (double)random.nextFloat() * ((double)1.0F - d), (double)pos.getZ() + (double)0.13125F + (double)0.7375F * (double)random.nextFloat(), g, h, j);
        }

    }

    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return LEVEL_TO_COLLISION_SHAPE[state.getValue(LEVEL)];
    }

    public VoxelShape getInteractionShape(BlockState state, BlockGetter world, BlockPos pos) {
        return RAYCAST_SHAPE;
    }

    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return LEVEL_TO_COLLISION_SHAPE[0];
    }

    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
        if (state.getValue(LEVEL) == MAX_LEVEL) {
            world.scheduleTick(pos, state.getBlock(), 20);
        }

    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        int i = state.getValue(LEVEL);
        if (i < MAX_LEVEL + 1 && canIncrease(stack)) {
            if (i < MAX_LEVEL && !world.isClientSide()) {
                BlockState blockState = addToComposter(player, state, world, pos, stack);
                world.levelEvent(1500, pos, state != blockState ? 1 : 0);
                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, world, pos, player, hand, hit);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        int i = state.getValue(LEVEL);
        if (i == MAX_LEVEL + 1) {
            if (!world.isClientSide()) {
                emptyFullComposter(player, state, world, pos);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public static BlockState compost(Entity user, BlockState state, ServerLevel world, ItemStack stack, BlockPos pos) {
        int i = state.getValue(LEVEL);
        if (i < MAX_LEVEL && canIncrease(stack)) {
            BlockState blockState = addToComposter(user, state, world, pos, stack);
            stack.shrink(1);
            return blockState;
        } else {
            return state;
        }
    }

    public static void emptyFullComposter(Entity user, BlockState state, Level world, BlockPos pos) {
        if (!world.isClientSide()) {
            Vec3 vec3d = Vec3.atLowerCornerWithOffset(pos, 0.5F, 1.01, 0.5F).offsetRandom(world.random, 0.7F);
	        ItemEntity itemEntity = new ItemEntity(world, vec3d.x(), vec3d.y(), vec3d.z(), new ItemStack(ResultItem, state.getValue(COCOON_COUNT)));
            itemEntity.setDefaultPickUpDelay();
            world.addFreshEntity(itemEntity);
        }

        BlockState blockState = emptyComposter(user, state, world, pos);
        world.playSound(null, pos, SoundEvents.COMPOSTER_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    static BlockState emptyComposter(@Nullable Entity user, BlockState state, LevelAccessor world, BlockPos pos) {
	    BlockState blockState = state.setValue(LEVEL, 0).setValue(COCOON_COUNT, 0);
	    world.setBlock(pos, blockState, 3);
	    world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(user, blockState));
	    return blockState;
    }

	static void setComposterItemCount(@Nullable Entity user, BlockState state, LevelAccessor world, BlockPos pos, int count) {
		if (count == 0) {
			emptyComposter(user, state, world, pos);
		} else {
			BlockState blockState = state.setValue(LEVEL, state.getValue(LEVEL)).setValue(COCOON_COUNT, count);
			world.setBlock(pos, blockState, 3);
			world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(user, blockState));
		}
    }

    static BlockState addToComposter(@Nullable Entity user, BlockState state, LevelAccessor world, BlockPos pos, ItemStack stack) {
        int i = state.getValue(LEVEL);
        float f = getIncreaseChance(stack);
        if ((i != 0 || !(f > 0.0F)) && !(world.getRandom().nextDouble() < (double)f)) {
            return state;
        } else {
            int j = i + 1;
	        BlockState blockState = state.setValue(LEVEL, j).setValue(COCOON_COUNT, state.getValue(COCOON_COUNT));
            world.setBlock(pos, blockState, 3);
            world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(user, blockState));
            if (j == MAX_LEVEL) {
                world.scheduleTick(pos, state.getBlock(), 20);
            }

            return blockState;
        }
    }

    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (state.getValue(LEVEL) == MAX_LEVEL) {
            int count = ResultCount.apply(random);
	        world.setBlock(pos, state.setValue(LEVEL, MAX_LEVEL + 1).setValue(COCOON_COUNT, count), 3);
            world.playSound(null, pos, SoundEvents.COMPOSTER_READY, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

    }

    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        return state.getValue(LEVEL);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
	    builder.add(LEVEL, COCOON_COUNT);
    }

    public boolean canPathfindThrough(BlockState state, BlockGetter world, BlockPos pos, PathComputationType type) {
        return false;
    }

    public WorldlyContainer getContainer(BlockState state, LevelAccessor world, BlockPos pos) {
        int i = state.getValue(LEVEL);
        if (i == MAX_LEVEL + 1) {
	        return new WebComposterBlock.FullComposterInventory(state, world, pos, new ItemStack(ResultItem, state.getValue(COCOON_COUNT)));
        } else {
            return i < MAX_LEVEL ? new ComposterInventory(state, world, pos) : new DummyInventory();
        }
    }

    static class DummyInventory extends SimpleContainer implements WorldlyContainer {
        public DummyInventory() {
            super(0);
        }

        public int[] getSlotsForFace(Direction side) {
            return new int[0];
        }

        public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
            return false;
        }

        public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
            return false;
        }
    }

    static class FullComposterInventory extends SimpleContainer implements WorldlyContainer {
        private final BlockState state;
        private final LevelAccessor world;
        private final BlockPos pos;
        private boolean dirty;

        public FullComposterInventory(BlockState state, LevelAccessor world, BlockPos pos, ItemStack outputItem) {
            super(outputItem);
            this.state = state;
            this.world = world;
            this.pos = pos;
        }

        public int getMaxStackSize() {
	        return 64;
        }

        public int[] getSlotsForFace(Direction side) {
            return side == Direction.DOWN ? new int[]{0} : new int[0];
        }

        public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
            return false;
        }

        public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
            return !this.dirty && dir == Direction.DOWN && stack.is(ResultItem);
        }

        public void setChanged() {
	        this.dirty = true;
	        if (this.getItem(0).isEmpty()) {
		        WebComposterBlock.emptyComposter(null, this.state, this.world, this.pos);
	        } else {
		        WebComposterBlock.setComposterItemCount(null, this.state, this.world, this.pos, this.getItem(0).getCount());
	        }
        }
    }

    static class ComposterInventory extends SimpleContainer implements WorldlyContainer {
        private final BlockState state;
        private final LevelAccessor world;
        private final BlockPos pos;
        private boolean dirty;

        public ComposterInventory(BlockState state, LevelAccessor world, BlockPos pos) {
            super(1);
            this.state = state;
            this.world = world;
            this.pos = pos;
        }

        public int getMaxStackSize() {
            return 1;
        }

        public int[] getSlotsForFace(Direction side) {
            return side == Direction.UP ? new int[]{0} : new int[0];
        }

        public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
            return !this.dirty && dir == Direction.UP && WebComposterBlock.canIncrease(stack);
        }

        public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
            return false;
        }

        public void setChanged() {
            ItemStack itemStack = this.getItem(0);
            if (!itemStack.isEmpty()) {
                this.dirty = true;
                BlockState blockState = WebComposterBlock.addToComposter(null, this.state, this.world, this.pos, itemStack);
                this.world.levelEvent(1500, this.pos, blockState != this.state ? 1 : 0);
                this.removeItemNoUpdate(0);
            }

        }
    }
}
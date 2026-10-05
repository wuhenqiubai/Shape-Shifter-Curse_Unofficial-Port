package net.onixary.shapeShifterCurseFabric.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.onixary.shapeShifterCurseFabric.blocks.block_entity.AltarBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


// 渲染先用透明方案吧 BlockEntity类方块由BlockEntity动态渲染
public class AltarBlock extends BaseEntityBlock {
    // 1.21.11: DirectionProperty 已并入 EnumProperty<Direction>（BlockStateProperties.HORIZONTAL_FACING 的类型）
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    protected AltarBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AltarBlockEntity(pos, state);
    }


    // ⚠ 1.21.11 的 BlockBehaviour.useWithoutItem 是【5 参数、protected】（1.21.1 带 InteractionHand 且为 public）。
    // 参数签名不符就不会构成 override —— 编译器不报错，但右键会静默走基类默认实现（返回 PASS，表现为毫无反应）。
    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        } else {
            this.openScreen(world, pos, player);
            return InteractionResult.CONSUME;
        }
    }

    protected void openScreen(Level world, BlockPos pos, Player player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof AltarBlockEntity altarBlockEntity) {
            altarBlockEntity.lastUser = player.getUUID();
            player.openMenu(altarBlockEntity);
        }
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // BaseEntityBlock 要求实现抽象的 codec()（用于 Block 的注册/网络同步）
    @Override
    protected @NotNull MapCodec<AltarBlock> codec() {
        return Block.simpleCodec(AltarBlock::new);
    }

    // BaseEntityBlock.createTickerHelper 是本版本上的对应工具（那个本地 checkType 是 1.20.1 yarn 的
    // BlockWithEntity.checkType 遗骸，两个重载会互相打转，编译不过）
    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return world.isClientSide() ? null : createTickerHelper(type, RegCustomBlock.ALTAR_BLOCK_ENTITY, (world1, pos, blockState, blockEntity) -> blockEntity.tick(world1, pos, blockState, blockEntity));
    }

    // 1.21.1 侧新增（1.21.11 侧原先缺这段）：破坏祭坛时把内部物品掉出来，否则会静默吞掉。
    // 1.21.11: BlockBehaviour.onRemove(...) 已被 affectNeighborsAfterRemoval(BlockState, ServerLevel, BlockPos, boolean) 取代。
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof AltarBlockEntity altarBlockEntity) {
            Containers.dropContents(world, pos, altarBlockEntity);
        }
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }
}

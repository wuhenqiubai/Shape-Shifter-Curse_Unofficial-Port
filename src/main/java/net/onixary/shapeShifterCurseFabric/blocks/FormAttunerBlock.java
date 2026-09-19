package net.onixary.shapeShifterCurseFabric.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.onixary.shapeShifterCurseFabric.blocks.block_entity.FormAttunerBlockEntity;
import net.onixary.shapeShifterCurseFabric.networking.ModPacketsS2CServer;
import net.onixary.shapeShifterCurseFabric.util.util.CachedDataMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class FormAttunerBlock extends BaseEntityBlock implements BeaconBeamBlock {
    // 1.21.1 Mojmap：Entity.getUuid() → getUUID()。
    // （上游 1.20.1 版本这里写的是 BaseEntityBlock::getUuid —— 接收者本身就是错的，
    //   CachedDataMap 在运行期会因此抛 IllegalStateException: keySupplier is null!）
    public static final CachedDataMap<UUID, Player, BlockPos> playerLastAttunerPos = new CachedDataMap<>((uuid -> null), Entity::getUUID);

    public static @Nullable FormAttunerBlockEntity getPlayerLastUsedAttuner(Player player) {
        BlockPos pos = playerLastAttunerPos.get(player);
        if (pos == null) {
            return null;
        }
        // 检查是否被加载：Yarn 的 Level.isChunkLoaded(x, z) → Mojmap Level.isLoaded(BlockPos)
        if (!player.level().isLoaded(pos)) {
            return null;
        }
        return player.level().getBlockEntity(pos) instanceof FormAttunerBlockEntity ? (FormAttunerBlockEntity) player.level().getBlockEntity(pos) : null;
    }

    protected FormAttunerBlock(BlockBehaviour.Properties settings) {
        super(settings);
    }

    // Yarn getRenderType → Mojmap getRenderShape。
    // ⚠ 上游版本这里**漏了 @Override**，所以编译器不报错 —— 但方法名对不上就等于没覆盖，
    //   BaseEntityBlock 默认返回 RenderShape.INVISIBLE，方块本体根本不渲染。补 @Override 让编译器守住。
    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // Yarn createBlockEntity → Mojmap newBlockEntity
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FormAttunerBlockEntity(pos, state);
    }

    @Override
    public @NotNull DyeColor getColor() {
        return DyeColor.PURPLE;
    }

    // Yarn BlockWithEntity.checkType → Mojmap BaseEntityBlock.createTickerHelper。
    // ⚠ 注意**不能**照抄 AltarBlock 的 `world.isClientSide ? null : ...` 短路：
    //   光束分段数据是在 tick() 里算的，客户端不 tick 就没有光束可渲染。
    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, RegCustomBlock.FORM_ATTUNER_BLOCK_ENTITY, FormAttunerBlockEntity::tick);
    }

    // 1.21 的 BaseEntityBlock 要求实现抽象的 codec()
    @Override
    protected @NotNull MapCodec<FormAttunerBlock> codec() {
        return Block.simpleCodec(FormAttunerBlock::new);
    }

    // Yarn onUse → Mojmap useWithoutItem（1.21 起这个方法不再收 InteractionHand）
    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        } else {
            this.openScreen(world, pos, (ServerPlayer) player);
            return InteractionResult.CONSUME;
        }
    }

    protected void openScreen(Level world, BlockPos pos, ServerPlayer player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof FormAttunerBlockEntity formAttunerBlockEntity) {
            playerLastAttunerPos.setA(player, pos);
            ModPacketsS2CServer.sendOpenFormUpgradeMenu(player, formAttunerBlockEntity.level);
        }
    }
}

package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.blocks.RegCustomBlock;
import net.onixary.shapeShifterCurseFabric.blocks.TempWebBridgeBlock;
import net.onixary.shapeShifterCurseFabric.entity.projectile.WebBullet;

import java.util.function.Consumer;

public class WebBridgeAction {
    public record WebLadderConfig(int SideBlockNum, int BottomBlockNum, int TopBlockNum, boolean LargerLadder, float LargerLadderCountPercent) {}
    public record WebBridgeConfig(int Length, int Width) {}

    public static boolean SetWebBlock(Level world, BlockPos pos, Block WebBlock, Direction facing) {
        BlockState blockState = world.getBlockState(pos);
        if (blockState.isAir() || blockState.is(WebBlock)) {
            BlockState state = WebBlock.defaultBlockState().setValue(TempWebBridgeBlock.HORIZONTAL_FACING, facing);
            world.setBlockAndUpdate(pos, state);
            return true;
        }
        return false;
    }

    public static void BuildWebLadder(Level world, BlockHitResult blockHitResult, WebLadderConfig config, Block LadderBlock) {
        BlockPos pos = blockHitResult.getBlockPos();
        Direction direction = blockHitResult.getDirection();

        Direction[] horizontalDirections = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        RandomSource random = world.getRandom();

        BlockPos NowPos = null;
        Direction LadderDirection = null;
        int Length = 0;
        boolean LargerLadder = config.LargerLadder;

        switch (direction) {
            case UP -> {
                NowPos = pos.above();
                LadderDirection = Direction.UP;
                Length = config.TopBlockNum;
            }
            case DOWN -> {
                NowPos = pos.below();
                LadderDirection = Direction.DOWN;
                Length = config.BottomBlockNum;
            }
            case NORTH, WEST, EAST, SOUTH -> {
                NowPos = pos.relative(direction);
                LadderDirection = Direction.DOWN;
                Length = config.SideBlockNum;
            }
        }

        int LargerLadderCount = (int)(config.LargerLadderCountPercent * Length);

        for (int i = 0; i < Length; i++) {
            Direction randomFacing = horizontalDirections[random.nextInt(horizontalDirections.length)];
            if (!SetWebBlock(world, NowPos, LadderBlock, randomFacing)) {
                break;
            }
            if (LargerLadder && LargerLadderCount > 0) {
                randomFacing = horizontalDirections[random.nextInt(horizontalDirections.length)];
                SetWebBlock(world, NowPos.east(), LadderBlock, randomFacing);
                randomFacing = horizontalDirections[random.nextInt(horizontalDirections.length)];
                SetWebBlock(world, NowPos.west(), LadderBlock, randomFacing);
                randomFacing = horizontalDirections[random.nextInt(horizontalDirections.length)];
                SetWebBlock(world, NowPos.north(), LadderBlock, randomFacing);
                randomFacing = horizontalDirections[random.nextInt(horizontalDirections.length)];
                SetWebBlock(world, NowPos.south(), LadderBlock, randomFacing);
                LargerLadderCount--;
            }
            NowPos = NowPos.relative(LadderDirection);
        }
    }

    public static void BuildWebBridge(Level world, BlockPos pos, Direction direction, WebBridgeConfig config, Block WebBlock) {
        BlockPos NowPos = pos;
        BlockPos TempPos = pos;
        Direction TempDirection = direction;
        if (direction == Direction.UP || direction == Direction.DOWN) {
            return;
        }
        // 预定义水平方向数组，用于随机选择
        Direction[] horizontalDirections = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        RandomSource random = world.getRandom();
        
        for (int k = -config.Width; k <= config.Width; k++) {
            for (int m = -config.Width; m <= config.Width; m++) {
                Direction randomFacing = horizontalDirections[random.nextInt(horizontalDirections.length)];
                SetWebBlock(world, pos.offset(k, 0, m), WebBlock, randomFacing);
            }
        }
        for (int i = 0; i < config.Length; i++) {
            Direction randomFacing = horizontalDirections[random.nextInt(horizontalDirections.length)];
            SetWebBlock(world, NowPos, WebBlock, randomFacing);
            TempPos = NowPos;
            TempDirection = direction.getClockWise();
            for (int j = 0; j < config.Width; j++) {
                TempPos = TempPos.relative(TempDirection);
                randomFacing = horizontalDirections[random.nextInt(horizontalDirections.length)];
                SetWebBlock(world, TempPos, WebBlock, randomFacing);
            }
            TempPos = NowPos;
            TempDirection = direction.getCounterClockWise();
            for (int j = 0; j < config.Width; j++) {
                TempPos = TempPos.relative(TempDirection);
                randomFacing = horizontalDirections[random.nextInt(horizontalDirections.length)];
                SetWebBlock(world, TempPos, WebBlock, randomFacing);
            }
            NowPos = NowPos.relative(direction);
        }
    }

    public static void registerAction(Consumer<ActionFactory<Entity>> ActionRegister, Consumer<ActionFactory<Tuple<Entity, Entity>>> BIActionRegister) {
        ActionRegister.accept(new ActionFactory<>(
                ShapeShifterCurseFabric.identifier("web_bridge"),
                new SerializableData()
                        .add("web_bridge_length", SerializableDataTypes.INT, 16)
                        .add("web_bridge_width", SerializableDataTypes.INT, 0),
                (data, entity) -> {
                    BlockPos pos = entity.blockPosition().below();
                    if (entity.isShiftKeyDown()) {
                        pos = pos.above();
                        // 如果需要俯仰角控制就取消下面的注释 并且把上面的 "pos = pos.up();" 给注释掉
                        // Vec3d player_pos = entity.getPos();
                        // if (player_pos.getY() - pos.up().getY() > 0.025) {
                        //     // 不完整方块
                        //     pos = pos.up();
                        // }
                        // float pitch = entity.getPitch();
                        // // 俯仰角取值 -90 ~ 90
                        // if (pitch > 30.0f) {
                        //     pos = pos.down();
                        // } else if (pitch < -30.0f) {
                        //     pos = pos.up();
                        // }
                    }
                    Direction direction = entity.getDirection();
                    BuildWebBridge(entity.level(), pos, direction, new WebBridgeConfig(data.getInt("web_bridge_length"), data.getInt("web_bridge_width")), RegCustomBlock.TEMP_WEB_BRIDGE);
                }
        ));

        ActionRegister.accept(new ActionFactory<>(
                ShapeShifterCurseFabric.identifier("fire_web_bullet"),
                new SerializableData()
                        .add("tier", SerializableDataTypes.INT, 1)
                        .add("divergence", SerializableDataTypes.FLOAT, 1F)
                        .add("speed", SerializableDataTypes.FLOAT, 1.5F)
                        .add("projectile_action", ApoliDataTypes.ENTITY_ACTION, null)
                        .add("enable_entangled_effect", SerializableDataTypes.BOOLEAN, true)
                        .add("enable_top_block_build", SerializableDataTypes.BOOLEAN, true),
                (data, entity) -> {
                    if (entity instanceof LivingEntity livingEntity) {
                        WebBullet webBullet = new WebBullet(livingEntity, data.getInt("tier"), data.getBoolean("enable_entangled_effect"), data.getBoolean("enable_top_block_build"));
                        webBullet.shootFromRotation(livingEntity, livingEntity.getXRot(), livingEntity.getYRot(), 0.0f, data.getFloat("speed"), data.getFloat("divergence"));
                        livingEntity.level().addFreshEntity(webBullet);
                        data.<Consumer<Entity>>ifPresent("projectile_action", projectileAction -> projectileAction.accept(webBullet));
                    }
                }
        ));
    }
}

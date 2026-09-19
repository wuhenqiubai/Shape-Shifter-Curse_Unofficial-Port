package net.onixary.shapeShifterCurseFabric.blocks.block_entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.onixary.shapeShifterCurseFabric.blocks.RegCustomBlock;
import net.onixary.shapeShifterCurseFabric.cursed_moon.CursedMoon;

import java.util.Arrays;
import java.util.List;

public class FormAttunerBlockEntity extends BlockEntity {
    public static int MAX_LEVEL = 4;  // 10级大约一次检查1770个方块 再大别整成卡服务机了 这玩意性能消耗指数级上升(但物资消耗会逐渐成为线性) 而且绿宝石块当前版本有掠夺塔(不止掠夺塔可以整 但这应该是效率较高的方式 其他方法感觉更卡服 比如超多核心刷铁塔(这个效率不太高) 双维度猪人塔(这个劲大 我以前试过)) 真要卡爆服务器很简单
    public int level = 0;
    private int minY = 0;
    private List<BeamSegment> beams = Lists.newArrayList();
    private List<BeamSegment> beamSegments = Lists.newArrayList();

    public static void allocMaxLevel(int maxLevel) {
        MAX_LEVEL = Math.max(MAX_LEVEL, maxLevel);
    }

    public FormAttunerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(RegCustomBlock.FORM_ATTUNER_BLOCK_ENTITY, blockPos, blockState);
    }

    public static void tick(Level world, BlockPos pos, BlockState state, FormAttunerBlockEntity blockEntity) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        BlockPos blockPos;
        if (blockEntity.minY < j) {
            blockPos = pos;
            blockEntity.beams = Lists.newArrayList();
            blockEntity.minY = pos.getY() - 1;
        } else {
            blockPos = new BlockPos(i, blockEntity.minY + 1, k);
        }
        BeamSegment beamSegment = blockEntity.beams.isEmpty() ? null : (BeamSegment)blockEntity.beams.get(blockEntity.beams.size() - 1);
        int l = world.getHeight(Heightmap.Types.WORLD_SURFACE, i, k);
        for(int m = 0; m < 10 && blockPos.getY() <= l; ++m) {
            BlockState blockState = world.getBlockState(blockPos);
            Block block = blockState.getBlock();
            if (block instanceof BeaconBeamBlock) {
                // 1.21.1 Mojmap：DyeColor 的颜色分量取法由 Yarn 的 getColorComponents()（float[]）
                // 改为 getTextureDiffuseColor()（ARGB int），这里拆成 0..1 的分量给 BeamSegment。
                int argb = ((BeaconBeamBlock)block).getColor().getTextureDiffuseColor();
                float[] fs = new float[]{
                    FastColor.ARGB32.red(argb) / 255.0F,
                    FastColor.ARGB32.green(argb) / 255.0F,
                    FastColor.ARGB32.blue(argb) / 255.0F
                };
                if (blockEntity.beams.size() <= 1) {
                    beamSegment = new BeamSegment(fs);
                    blockEntity.beams.add(beamSegment);
                } else if (beamSegment != null) {
                    if (Arrays.equals(fs, beamSegment.color)) {
                        beamSegment.increaseHeight();
                    } else {
                        beamSegment = new BeamSegment(new float[]{(beamSegment.color[0] + fs[0]) / 2.0F, (beamSegment.color[1] + fs[1]) / 2.0F, (beamSegment.color[2] + fs[2]) / 2.0F});
                        blockEntity.beams.add(beamSegment);
                    }
                }
            } else {
                if (beamSegment == null || blockState.getLightBlock(world, blockPos) >= 15 && !blockState.is(Blocks.BEDROCK)) {
                    blockEntity.beams.clear();
                    blockEntity.minY = l;
                    break;
                }

                beamSegment.increaseHeight();
            }

            blockPos = blockPos.above();
            ++blockEntity.minY;
        }

        int m = blockEntity.level;
        if (world.getGameTime() % 80L == 0L) {
            if (!blockEntity.beamSegments.isEmpty()) {
                blockEntity.level = updateLevel(world, i, j, k);
            }
            if (blockEntity.level > 0 && !blockEntity.beamSegments.isEmpty()) {
                // 这里可以写加 Buff
                playSound(world, pos, SoundEvents.BEACON_AMBIENT);
            }
        }
        if (blockEntity.minY >= l) {
            blockEntity.minY = world.getMinBuildHeight() - 1;
            boolean bl = m > 0;
            blockEntity.beamSegments = blockEntity.beams;
            if (!world.isClientSide) {
                boolean bl2 = blockEntity.level > 0;
                if (!bl && bl2) {
                    playSound(world, pos, SoundEvents.BEACON_ACTIVATE);
                } else if (bl && !bl2) {
                    playSound(world, pos, SoundEvents.BEACON_DEACTIVATE);
                }
            }
        }
    }

    private static int updateLevel(Level world, int x, int y, int z) {
        // 设定上需要诅咒之月的力量 So 仅主世界可用
        if (world.dimension() != Level.OVERWORLD) {
            return 0;
        }
        if (!CursedMoon.isInCursedMoon(world)) {
            return 0;
        }
        int i = 0;
        for(int j = 1; j <= MAX_LEVEL; i = j++) {
            int k = y - j;
            if (k < world.getMinBuildHeight()) {
                break;
            }
            boolean bl = true;
            for(int l = x - j; l <= x + j && bl; ++l) {
                for(int m = z - j; m <= z + j; ++m) {
                    if (!world.getBlockState(new BlockPos(l, k, m)).is(BlockTags.BEACON_BASE_BLOCKS)) {
                        bl = false;
                        break;
                    }
                }
            }
            if (!bl) {
                break;
            }
        }
        return i;
    }

    public List<FormAttunerBlockEntity.BeamSegment> getBeamSegments() {
        return this.level == 0 ? ImmutableList.of() : this.beamSegments;
    }
    
    public static void playSound(Level world, BlockPos pos, SoundEvent sound) {
        world.playSound((Player)null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
    
    @Override
    public void setRemoved() {
        // 注意：本类自己有个 `int level` 字段，遮蔽了 BlockEntity.level，所以用 getLevel() 取所在维度
        playSound(this.getLevel(), this.worldPosition, SoundEvents.BEACON_DEACTIVATE);
        super.setRemoved();
    }

    @Override
    public void setLevel(Level world) {
        super.setLevel(world);
        this.minY = world.getMinBuildHeight() - 1;
    }

    public static class BeamSegment {
        final float[] color;
        private int height;
        public BeamSegment(float[] color) {
            this.color = color;
            this.height = 1;
        }
        
        public void increaseHeight() {
            ++this.height;
        }
        public float[] getColor() {
            return this.color;
        }

        public int getHeight() {
            return this.height;
        }
    }
}

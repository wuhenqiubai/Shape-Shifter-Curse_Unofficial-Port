package net.onixary.shapeShifterCurseFabric.minion;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.minion.mobs.AnubisWolfMinionEntity;
import net.onixary.shapeShifterCurseFabric.util.EntityAttributeRegister;
import org.jetbrains.annotations.Nullable;

public class MinionRegister {
    public static final EntityType<AnubisWolfMinionEntity> ANUBIS_WOLF_MINION = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            AnubisWolfMinionEntity.MinionID,
            FabricEntityTypeBuilder
                    .create(MobCategory.MISC, AnubisWolfMinionEntity::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f))
                    .build()
    );

    public static void register() {
        EntityAttributeRegister.register(ANUBIS_WOLF_MINION, AnubisWolfMinionEntity::createWolfMinionAttributes);
    }


    public static void DisSpawnAllMinion(Player player) {
        if (player instanceof IPlayerEntityMinion minionPlayer) {
            minionPlayer.shape_shifter_curse$clearAllMinions();
        }
    }

    public static @Nullable <T extends LivingEntity> T SpawnMinion(EntityType<T> minion, ServerLevel world, BlockPos pos, ServerPlayer player) {
        T entity = minion.spawn(world, pos, MobSpawnType.NATURAL);
        if (entity instanceof IMinion<?> minionEntity) {
            minionEntity.InitMinion(player);
            return entity;
        }
        return null;
    }

    public static void SetCoolDown(Identifier MinionID, Player player) {
        if (player instanceof IPlayerEntityMinion minionPlayer) {
            minionPlayer.shape_shifter_curse$applyCooldown(MinionID, player.tickCount);
        }
    }

    public static boolean IsInCoolDown(Identifier MinionID, Player player, int Cooldown) {
        if (Cooldown <= 0) {
            return false;
        }
        if (player instanceof IPlayerEntityMinion minionPlayer) {
            long LastCooldown = minionPlayer.shape_shifter_curse$getCooldownTime(MinionID);
            if (LastCooldown == 0) {  // 没召唤过
                return false;
            }
            if (LastCooldown > player.tickCount) {
                minionPlayer.shape_shifter_curse$applyCooldown(MinionID, 0);  // player.age会刷新
                return false;
            }
            return LastCooldown + Cooldown >= player.tickCount;
        }
        return true;
    }

    public static void ResetPlayerCoolDown(Player player) {
        if (player instanceof IPlayerEntityMinion minionPlayer) {
            minionPlayer.shape_shifter_curse$resetAllCooldown();
        }
    }

    private static boolean IsSpaceEmpty(Level world, BlockPos pos) {
        return world.isEmptyBlock(pos) || world.isWaterAt(pos);
    }

    private static boolean IsSpaceEmpty(Level world, BlockPos pos, int height) {
        for (int i = 0; i < height; i++) {
            if (!IsSpaceEmpty(world, pos)) {
                return false;
            }
            pos = pos.above();
        }
        return true;
    }

    private static int RandomInt(RandomSource randomSource, int min, int max) {
        return randomSource.nextInt(max - min + 1) + min;
    }

    public static @Nullable BlockPos getNearbyEmptySpace(Level world, RandomSource randomSource, BlockPos startPos, int XZRange, int YRange, int SpaceHeight, int MaxTry) {
        for (int i = 0; i < MaxTry; i++) {
            int x = RandomInt(randomSource, -XZRange, XZRange);
            int z = RandomInt(randomSource, -XZRange, XZRange);
            int y = RandomInt(randomSource, -YRange, YRange);
            BlockPos pos = startPos.offset(x, y, z);
            for (int j = 0; j < YRange; j++) {
                if (!IsSpaceEmpty(world, pos)) {
                    pos = pos.above();
                }
                else if (IsSpaceEmpty(world, pos.below())) {
                    pos = pos.below();
                }
                else {
                    break;
                }
            }
            if (IsSpaceEmpty(world, pos, SpaceHeight)) {
                return pos;
            }
            continue;
        }
        return null;
    }
}
package io.github.apace100.apoli.util;

import com.google.gson.JsonSyntaxException;
import io.github.apace100.apoli.data.DamageSourceDescription;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

public final class MiscUtil {

    public static Optional<Entity> getEntityWithPassengers(Level world, EntityType<?> entityType, @Nullable CompoundTag entityNbt, Vec3 pos, float yaw, float pitch) {

        if (world.isClientSide) return Optional.empty();
        ServerLevel serverWorld = (ServerLevel) world;

        CompoundTag entityToSpawnNbt = new CompoundTag();
        if (entityNbt != null) entityToSpawnNbt.merge(entityNbt);
        entityToSpawnNbt.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString());

        Entity entityToSpawn = EntityType.loadEntityRecursive(
            entityToSpawnNbt,
            serverWorld,
            entity -> {
                entity.absMoveTo(pos.x, pos.y, pos.z, yaw, pitch);
                return entity;
            }
        );
        if (entityToSpawn == null) return Optional.empty();

        if (entityNbt == null && entityToSpawn instanceof Mob mobToSpawn) mobToSpawn.finalizeSpawn(
            serverWorld,
            serverWorld.getCurrentDifficultyAt(BlockPos.containing(pos)),
            MobSpawnType.COMMAND,
            null
        );
        return Optional.of(entityToSpawn);

    }

    public static BlockState getInWallBlockState(LivingEntity playerEntity) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for(int i = 0; i < 8; ++i) {
            double d = playerEntity.getX() + (double)(((float)((i >> 0) % 2) - 0.5F) * playerEntity.getBbWidth() * 0.8F);
            double e = playerEntity.getEyeY() + (double)(((float)((i >> 1) % 2) - 0.5F) * 0.1F);
            double f = playerEntity.getZ() + (double)(((float)((i >> 2) % 2) - 0.5F) * playerEntity.getBbWidth() * 0.8F);
            mutable.set(d, e, f);
            BlockState blockState = playerEntity.level().getBlockState(mutable);
            if (blockState.getRenderShape() != RenderShape.INVISIBLE && blockState.isViewBlocking(playerEntity.level(), mutable)) {
                return blockState;
            }
        }

        return null;
    }

    public static <T> Predicate<T> combineOr(Predicate<T> a, Predicate<T> b) {
        if(a == null) {
            return b;
        }
        if(b == null) {
            return a;
        }
        return a.or(b);
    }

    public static <T> Predicate<T> combineAnd(Predicate<T> a, Predicate<T> b) {
        if(a == null) {
            return b;
        }
        if(b == null) {
            return a;
        }
        return a.and(b);
    }

    public static DamageSource createDamageSource(DamageSources damageSources,
                                                  @Nullable DamageSourceDescription damageSourceDescription,
                                                  @Nullable ResourceKey<DamageType> damageType) {
        if(damageSourceDescription == null && damageType == null) {
            throw new JsonSyntaxException("Either a legacy damage source or an ID of a damage type must be specified");
        }
        return damageSourceDescription == null ? damageSources.source(damageType) : damageSourceDescription.create(damageSources);
    }

    public static DamageSource createDamageSource(DamageSources damageSources,
                                                  @Nullable DamageSourceDescription damageSourceDescription,
                                                  @Nullable ResourceKey<DamageType> damageType, Entity attacker) {
        if(damageSourceDescription == null && damageType == null) {
            throw new JsonSyntaxException("Either a legacy damage source or an ID of a damage type must be specified");
        }
        return damageSourceDescription == null ? damageSources.source(damageType, attacker) : damageSourceDescription.create(damageSources, attacker);
    }

    public static DamageSource createDamageSource(DamageSources damageSources,
                                                  @Nullable DamageSourceDescription damageSourceDescription,
                                                  @Nullable ResourceKey<DamageType> damageType, Entity source, Entity attacker) {
        if(damageSourceDescription == null && damageType == null) {
            throw new JsonSyntaxException("Either a legacy damage source or an ID of a damage type must be specified");
        }
        return damageSourceDescription == null ? damageSources.source(damageType, source, attacker) : damageSourceDescription.create(damageSources, source, attacker);
    }
}

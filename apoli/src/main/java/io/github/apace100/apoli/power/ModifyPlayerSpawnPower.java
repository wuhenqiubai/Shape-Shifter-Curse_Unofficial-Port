package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Tuple;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.function.TriFunction;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ModifyPlayerSpawnPower extends Power {

    public final ResourceKey<Level> dimension;
    public final float dimensionDistanceMultiplier;
    public final ResourceLocation biomeId;
    public final SpawnStrategy spawnStrategy;
    public final ResourceKey<Structure> structure;
    public final SoundEvent spawnSound;

    private enum SpawnStrategy {

        CENTER((blockPos, center, multiplier) -> new BlockPos(0, center, 0)),
        DEFAULT(
            (blockPos, center, multiplier) -> {

                BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

                if (multiplier != 0) mut.set(blockPos.getX() * multiplier, blockPos.getY(), blockPos.getZ() * multiplier);
                else mut.set(blockPos);

                return mut;

            }
        );

        final TriFunction<BlockPos, Integer, Float, BlockPos> strategyApplier;
        SpawnStrategy(TriFunction<BlockPos, Integer, Float, BlockPos> strategyApplier) {
            this.strategyApplier = strategyApplier;
        }

        public BlockPos apply(BlockPos blockPos, int center, float multiplier) {
            return strategyApplier.apply(blockPos, center, multiplier);
        }

    }

    public ModifyPlayerSpawnPower(PowerType<?> type, LivingEntity entity, ResourceKey<Level> dimension, float dimensionDistanceMultiplier, ResourceLocation biomeId, SpawnStrategy spawnStrategy, ResourceKey<Structure> structure, SoundEvent spawnSound) {
        super(type, entity);
        this.dimension = dimension;
        this.dimensionDistanceMultiplier = dimensionDistanceMultiplier;
        this.biomeId = biomeId;
        this.spawnStrategy = spawnStrategy;
        this.structure = structure;
        this.spawnSound = spawnSound;
    }

    @Override
    public void onRemoved() {

        if (entity.level().isClientSide || !(entity instanceof Player playerEntity)) return;

        ServerPlayer serverPlayerEntity = (ServerPlayer) playerEntity;
        if (serverPlayerEntity.hasDisconnected() || serverPlayerEntity.getRespawnPosition() == null || !serverPlayerEntity.isRespawnForced()) return;

        serverPlayerEntity.setRespawnPosition(Level.OVERWORLD, null, 0F, false, false);

    }

    public void teleportToModifiedSpawn() {

        if (entity.level().isClientSide || !(entity instanceof Player playerEntity)) return;

        ServerPlayer serverPlayerEntity = (ServerPlayer) playerEntity;
        Tuple<ServerLevel, BlockPos> newSpawn = getSpawn(false);

        if (newSpawn == null) return;
        ServerLevel newSpawnDimension = newSpawn.getA();
        BlockPos newSpawnPos = newSpawn.getB();

        Vec3 tpPos = DismountHelper.findSafeDismountLocation(playerEntity.getType(), newSpawn.getA(), newSpawn.getB(), true);
        if (tpPos == null) {
            serverPlayerEntity.teleportTo(newSpawnDimension, newSpawnPos.getX(), newSpawnPos.getY(), newSpawnPos.getZ(), entity.getXRot(), entity.getYRot());
            Apoli.LOGGER.warn("Power {} could not find a suitable spawnpoint for {}! Teleporting to the desired location directly...", this.getType().getIdentifier(), entity.getScoreboardName());
        }

        else serverPlayerEntity.teleportTo(newSpawnDimension, tpPos.x, tpPos.y, tpPos.z, entity.getXRot(), entity.getYRot());

    }

    public Tuple<ServerLevel, BlockPos> getSpawn(boolean isSpawnObstructed) {

        if (entity.level().isClientSide || !(entity instanceof Player playerEntity)) return null;

        ServerPlayer serverPlayerEntity = (ServerPlayer) playerEntity;
        MinecraftServer server = serverPlayerEntity.getServer();
        if (server == null) return null;

        ServerLevel overworldDimension = server.getLevel(Level.OVERWORLD);
        if (overworldDimension == null) return null;

        ServerLevel targetDimension = server.getLevel(dimension);
        if (targetDimension == null) {
            Apoli.LOGGER.warn("Power {} could not set {}'s spawnpoint at dimension \"{}\" as it's not registered! Falling back to default spawnpoint...", this.getType().getIdentifier(), entity.getScoreboardName(), dimension.location());
            return null;
        }

        int center = targetDimension.getLogicalHeight() / 2;
        int range = 64;

        AtomicReference<Vec3> modifiedSpawnPos = new AtomicReference<>();

        BlockPos regularSpawnBlockPos = overworldDimension.getSharedSpawnPos();
        BlockPos.MutableBlockPos modifiedSpawnBlockPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos dimensionSpawnPos = spawnStrategy.apply(regularSpawnBlockPos, center, dimensionDistanceMultiplier).mutable();

        getBiomePos(targetDimension, dimensionSpawnPos).ifPresent(dimensionSpawnPos::set);
        getSpawnPos(targetDimension, dimensionSpawnPos, range).ifPresent(modifiedSpawnPos::set);

        if (modifiedSpawnPos.get() == null) return null;

        Vec3 msp = modifiedSpawnPos.get();
        modifiedSpawnBlockPos.set(msp.x, msp.y, msp.z);
        targetDimension.getChunkSource().addRegionTicket(TicketType.START, new ChunkPos(modifiedSpawnBlockPos), 11, Unit.INSTANCE);

        return new Tuple<>(targetDimension, modifiedSpawnBlockPos);

    }

    private Optional<BlockPos> getBiomePos(ServerLevel targetDimension, BlockPos originPos) {

        if (biomeId == null) return Optional.empty();

        Optional<Biome> targetBiome = targetDimension.registryAccess().registryOrThrow(Registries.BIOME).getOptional(biomeId);
        if (targetBiome.isEmpty()) {
            Apoli.LOGGER.warn("Power {} could not set {}'s spawnpoint at biome \"{}\" as it's not registered in dimension \"{}\".", this.getType().getIdentifier(), entity.getScoreboardName(), biomeId, dimension.location());
            return Optional.empty();
        }

        com.mojang.datafixers.util.Pair<BlockPos, Holder<Biome>> targetBiomePos = targetDimension.findClosestBiome3d(
            biome -> biome.value() == targetBiome.get(),
            originPos,
            6400,
            8,
            8
        );

        if (targetBiomePos != null) return Optional.of(targetBiomePos.getFirst());
        else {
            Apoli.LOGGER.warn("Power {} could not set {}'s spawnpoint at biome \"{}\" as it couldn't be found in dimension \"{}\".", this.getType().getIdentifier(), entity.getScoreboardName(), biomeId, dimension.location());
            return Optional.empty();
        }

    }

    private Optional<Tuple<BlockPos, Structure>> getStructurePos(Level world, ResourceKey<Structure> structure, TagKey<Structure> structureTag, ResourceKey<Level> dimension) {

        Registry<Structure> structureRegistry = world.registryAccess().registryOrThrow(Registries.STRUCTURE);
        HolderSet<Structure> structureRegistryEntryList = null;
        String structureTagOrName = "";

        if (structure != null) {

            var entry = structureRegistry.getHolder(structure);
            if (entry.isPresent()) structureRegistryEntryList = HolderSet.direct(entry.get());

            structureTagOrName = structure.location().toString();

        }

        if (structureRegistryEntryList == null) {

            var entryList = structureRegistry.getTag(structureTag);
            if (entryList.isPresent()) structureRegistryEntryList = entryList.get();

            structureTagOrName = "#" + structureTag.location().toString();

        }

        MinecraftServer server = entity.getServer();
        if (server == null) return Optional.empty();

        ServerLevel serverWorld = server.getLevel(dimension);
        if (serverWorld == null) return Optional.empty();

        BlockPos center = new BlockPos(0, 70, 0);
        com.mojang.datafixers.util.Pair<BlockPos, Holder<Structure>> structurePos = serverWorld
            .getChunkSource()
            .getGenerator()
            .findNearestMapStructure(
                serverWorld,
                structureRegistryEntryList,
                center,
                100,
                false
            );

        if (structurePos == null) {
            Apoli.LOGGER.warn("Power {} could not set {}'s spawnpoint at structure \"{}\" as it couldn't be found in dimension \"{}\".", this.getType().getIdentifier(), entity.getScoreboardName(), structureTagOrName, dimension.location());
            return Optional.empty();
        }

        else return Optional.of(new Tuple<>(structurePos.getFirst(), structurePos.getSecond().value()));

    }

    private Optional<Vec3> getSpawnPos(ServerLevel targetDimension, BlockPos originPos, int range) {

        if (structure == null) return getValidSpawn(targetDimension, originPos, range);

        Optional<Tuple<BlockPos, Structure>> targetStructure = getStructurePos(targetDimension, structure, null, dimension);
        if (targetStructure.isEmpty()) return Optional.empty();

        BlockPos targetStructurePos = targetStructure.get().getA();
        ChunkPos targetStructureChunkPos = new ChunkPos(targetStructurePos.getX() >> 4, targetStructurePos.getZ() >> 4);

        StructureStart targetStructureStart = targetDimension.structureManager().getStartForStructure(SectionPos.of(targetStructureChunkPos, 0), targetStructure.get().getB(), targetDimension.getChunk(targetStructurePos));
        if (targetStructureStart == null) return Optional.empty();

        BlockPos targetStructureCenter = new BlockPos(targetStructureStart.getBoundingBox().getCenter());
        return getValidSpawn(targetDimension, targetStructureCenter, range);

    }

    private Optional<Vec3> getValidSpawn(ServerLevel targetDimension, BlockPos startPos, int range) {

        //  The 'direction' vector that determines the direction of the iteration
        int dx = 1;
        int dz = 0;

        //  The length of the current segment
        int segmentLength = 1;

        //  The center of the structure/dimension
        int center = startPos.getY();

        //  The valid spawn position and (mutable) starting position
        Vec3 spawnPos;
        BlockPos.MutableBlockPos mutableStartPos = startPos.mutable();

        //  The current position
        int x = startPos.getX();
        int z = startPos.getZ();

        //  Determines how much of the current segment has been passed
        int segmentPassed = 0;

        //  Vertical offsets
        int upOffset = 0;
        int downOffset = 0;

        //  The min and max Y values of the target dimension
        int maxY = targetDimension.getLogicalHeight();
        int minY = targetDimension.dimensionTypeRegistration().value().minY();

        while (upOffset < maxY || downOffset > minY) {

            for (int steps = 0; steps < range; ++steps) {

                //  Make a step by adding the 'direction' vector to the current position
                x += dx;
                z += dz;
                mutableStartPos.setX(x);
                mutableStartPos.setZ(z);

                //  Increment how much of the current segment has been passed
                ++segmentPassed;

                //  Offset the Y axis (up and down) of the current position to check for valid spawn positions
                mutableStartPos.setY(center + upOffset);
                spawnPos = DismountHelper.findSafeDismountLocation(entity.getType(), targetDimension, mutableStartPos, true);
                if (spawnPos != null) return Optional.of(spawnPos);

                mutableStartPos.setY(center + downOffset);
                spawnPos = DismountHelper.findSafeDismountLocation(entity.getType(), targetDimension, mutableStartPos, true);
                if (spawnPos != null) return Optional.of(spawnPos);

                //  If the current segment has not been passed, continue the loop
                if (segmentPassed != segmentLength) continue;

                //  Otherwise, reset the value of how much of the current segment has been passed
                segmentPassed = 0;

                //  'Rotate' the 'direction' vector
                int bdx = dx;
                dx = -dz;
                dz = bdx;

                //  Increment the length of the current segment if necessary
                if (dz == 0) ++segmentLength;

            }

            //  Increment/decrement the up/down offsets until it's no longer less/greater than the max/min Y
            if (upOffset < maxY) upOffset++;
            if (downOffset > minY) downOffset--;

        }

        return Optional.empty();

    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("modify_player_spawn"),
            new SerializableData()
                .add("dimension", SerializableDataTypes.DIMENSION)
                .add("dimension_distance_multiplier", SerializableDataTypes.FLOAT, 0F)
                .add("biome", SerializableDataTypes.IDENTIFIER, null)
                .add("spawn_strategy", SerializableDataType.enumValue(SpawnStrategy.class), SpawnStrategy.DEFAULT)
                .add("structure", SerializableDataType.registryKey(Registries.STRUCTURE), null)
                .add("respawn_sound", SerializableDataTypes.SOUND_EVENT, null),
            data -> (powerType, livingEntity) -> new ModifyPlayerSpawnPower(
                powerType,
                livingEntity,
                data.get("dimension"),
                data.get("dimension_distance_multiplier"),
                data.get("biome"),
                data.get("spawn_strategy"),
                data.get("structure"),
                data.get("respawn_sound")
            )
        ).allowCondition();
    }

}
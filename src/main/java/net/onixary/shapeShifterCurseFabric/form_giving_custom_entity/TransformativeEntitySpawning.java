package net.onixary.shapeShifterCurseFabric.form_giving_custom_entity;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.axolotl.TransformativeAxolotlEntity;
import net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.bat.TransformativeBatEntity;
import net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.ocelot.TransformativeOcelotEntity;
import net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.wolf.TransformativeWolfEntity;

import java.util.HashMap;
import java.util.Map;

public class TransformativeEntitySpawning {
    public static void addEntitySpawns() {
        // T_OCELOT
        SpawnPlacements.register(
                ShapeShifterCurseFabric.T_OCELOT,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING,
                TransformativeOcelotEntity::canCustomSpawn
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(Biomes.JUNGLE)
                        .or(BiomeSelectors.includeByKey(Biomes.BAMBOO_JUNGLE)),
                MobCategory.MONSTER,
                ShapeShifterCurseFabric.T_OCELOT,
                10,
                1,
                3
        );
        // T_AXOLOTL
        SpawnPlacements.register(
                ShapeShifterCurseFabric.T_AXOLOTL,
                SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                TransformativeAxolotlEntity::canCustomSpawn
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(Biomes.LUSH_CAVES),
                MobCategory.AXOLOTLS,
                ShapeShifterCurseFabric.T_AXOLOTL,
                8,
                4,
                6
        );
        // T_BAT
        SpawnPlacements.register(
                ShapeShifterCurseFabric.T_BAT,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                TransformativeBatEntity::canCustomSpawn
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(BiomeTags.IS_OVERWORLD),
                MobCategory.AMBIENT,
                ShapeShifterCurseFabric.T_BAT,
                8,
                1,
                3
        );
        // T_WOLF
        SpawnPlacements.register(
                ShapeShifterCurseFabric.T_WOLF,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                TransformativeWolfEntity::canCustomSpawn
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(Biomes.DESERT),
                MobCategory.CREATURE,
                ShapeShifterCurseFabric.T_WOLF,
                2,  // 1/2 兔子的权重
                1,
                2
        );

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            // 1. 在沙漠神殿添加狼生成
            addStructureSpawn(
                server.overworld().registryAccess().registryOrThrow(Registries.STRUCTURE)
                    .get(ResourceLocation.fromNamespaceAndPath("minecraft", "desert_pyramid")),
                MobCategory.CREATURE,
                new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE,
                    WeightedRandomList.create(new MobSpawnSettings.SpawnerData(ShapeShifterCurseFabric.T_WOLF, 20, 3, 5)))
            );
            // 2. 在废弃矿井添加蜘蛛生成
            for (Holder<Structure> structureEntry : server.overworld().registryAccess().registryOrThrow(Registries.STRUCTURE)
                .getTagOrEmpty(TagKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath("minecraft", "mineshaft")))) {
                addStructureSpawn(
                    structureEntry.value(),
                    MobCategory.MONSTER,
                    new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE,
                        WeightedRandomList.create(new MobSpawnSettings.SpawnerData(ShapeShifterCurseFabric.T_SPIDER, 5, 1, 2)))
                );
            }
        });
    }

    // Structure.settings 是 protected final → AW 中已设置 accessible + mutable，可直接赋值
    private static void addStructureSpawn(Structure structure, MobCategory category, StructureSpawnOverride override) {
        if (structure == null) return;
        Map<MobCategory, StructureSpawnOverride> spawns = new HashMap<>(structure.spawnOverrides());
        spawns.put(category, override);
        Structure.StructureSettings newSettings = new Structure.StructureSettings.Builder(structure.biomes())
            .spawnOverrides(Map.copyOf(spawns))
            .generationStep(structure.step())
            .terrainAdapation(structure.terrainAdaptation())
            .build();
        structure.settings = newSettings;
    }
}

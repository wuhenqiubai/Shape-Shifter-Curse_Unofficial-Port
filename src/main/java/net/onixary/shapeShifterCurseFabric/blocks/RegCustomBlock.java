package net.onixary.shapeShifterCurseFabric.blocks;

import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;


public final class RegCustomBlock {
    public static final Block MOONDUST_CRYSTAL_GRIT = register("moondust_crystal_grit", new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.GRAVEL).mapColor(MapColor.COLOR_PURPLE).strength(0.6f, 0.6f).sound(SoundType.GRAVEL)));
    // TODO TEMP_WEB_BRIDGE 仅在测试时有物品 发布时记得用 registerWithOutItem
    public static final Block TEMP_WEB_BRIDGE = register("temp_web_bridge", new TempWebBridgeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(4.0f).randomTicks().noCollision().dynamicShape().noLootTable().isRedstoneConductor((state, getter, pos) -> false).ignitedByLava().sound(SoundType.WOOL)));

    public static final Block WEB_COMPOSTER = register("web_composter", new WebComposterBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.6F).sound(SoundType.AZALEA).noOcclusion()));
    public static final Block DEW_COVERED_COBWEB = register("dew_covered_cobweb", new DewCoveredCobwebBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).instrument(NoteBlockInstrument.BELL).strength(1.0F).sound(SoundType.WOOL).noCollision().noOcclusion()));

    public static void ClientInit() {
         BlockRenderLayerMap.putBlock(TEMP_WEB_BRIDGE, ChunkSectionLayer.CUTOUT);
         BlockRenderLayerMap.putBlock(WEB_COMPOSTER, ChunkSectionLayer.CUTOUT);
         BlockRenderLayerMap.putBlock(DEW_COVERED_COBWEB, ChunkSectionLayer.CUTOUT);
    }

    private static <T extends Block> T registerWithOutItem(String path, T block) {
        Registry.register(BuiltInRegistries.BLOCK, ResourceKey.create(BuiltInRegistries.BLOCK.key(), Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, path)), block);
        return block;
    }

    private static <T extends Block> T register(String path, T block) {
        Registry.register(BuiltInRegistries.BLOCK, ResourceKey.create(BuiltInRegistries.BLOCK.key(), Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, path)), block);
        Registry.register(BuiltInRegistries.ITEM, ResourceKey.create(BuiltInRegistries.ITEM.key(), Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, path)), new BlockItem(block, new Item.Properties()));
        return block;
    }

    public static void initialize() {
        // 蔓延速度=20, 燃烧速度=5，与木板相同
        FlammableBlockRegistry.getDefaultInstance().add(TEMP_WEB_BRIDGE, 60, 20);
    }
}
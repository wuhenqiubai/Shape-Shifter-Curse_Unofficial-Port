package net.onixary.shapeShifterCurseFabric.blocks;

import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.blocks.block_entity.AltarBlockEntity;
import net.onixary.shapeShifterCurseFabric.blocks.block_entity.FormAttunerBlockEntity;

import java.util.function.Function;

public final class RegCustomBlock {
    // 用 ofFullCopy 而非 of()：前者会连 gravel 的掉落物/爆炸抗性等一并继承，后者只给一份空白属性。
    // （合并上游时这行被改成了 of(Blocks.GRAVEL) —— 那是个不存在的重载 —— 后又降级成 of()，属性全丢。）
    public static final Block MOONDUST_CRYSTAL_GRIT = register("moondust_crystal_grit", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.GRAVEL).mapColor(MapColor.COLOR_PURPLE).strength(0.6f, 0.6f).sound(SoundType.GRAVEL));
    // TODO TEMP_WEB_BRIDGE 仅在测试时有物品 发布时记得用 registerWithOutItem
    public static final Block TEMP_WEB_BRIDGE = register("temp_web_bridge", TempWebBridgeBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(4.0f).randomTicks().noCollision().dynamicShape().noLootTable().isRedstoneConductor(Blocks::never).ignitedByLava().sound(SoundType.WOOL));

    public static final Block WEB_COMPOSTER = register("web_composter", WebComposterBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.6F).sound(SoundType.AZALEA).noOcclusion());
    public static final Block DEW_COVERED_COBWEB = register("dew_covered_cobweb", DewCoveredCobwebBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).instrument(NoteBlockInstrument.BELL).strength(1.0F).sound(SoundType.WOOL).noCollision().noOcclusion());

    // 合并 1.21.1：取「altar」改名（原 alter），但 API 保持 1.21.11 侧
    // （register 是工厂签名、FabricBlockEntityTypeBuilder、ChunkSectionLayer.CUTOUT）
    public static final Block Altar_BLOCK = register("altar", AltarBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).instrument(NoteBlockInstrument.BELL).strength(4.0F, 10.0F).sound(SoundType.AMETHYST).noOcclusion());
    public static final BlockEntityType<AltarBlockEntity> Altar_BLOCK_ENTITY = registerBlockEntity("altar_block_entity", FabricBlockEntityTypeBuilder.create(AltarBlockEntity::new, Altar_BLOCK).build());

    public static final Block FORM_ATTUNER_BLOCK = register("form_attuner", new FormAttunerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).instrument(NoteBlockInstrument.BELL).lightLevel((state) -> 15).strength(4.0F, 10.0F).sound(SoundType.GLASS).noOcclusion()));
    public static final BlockEntityType<FormAttunerBlockEntity> FORM_ATTUNER_BLOCK_ENTITY = registerBlockEntity("form_attuner_block_entity", BlockEntityType.Builder.of(FormAttunerBlockEntity::new, FORM_ATTUNER_BLOCK).build(null));

    public static void ClientInit() {
        // transparent透明模式不写Z，会出现自排序问题遮挡自己，只需要镂空的模型应该使用getCutout
        BlockRenderLayerMap.putBlock(TEMP_WEB_BRIDGE, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(WEB_COMPOSTER, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(DEW_COVERED_COBWEB, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(Altar_BLOCK, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(FORM_ATTUNER_BLOCK, ChunkSectionLayer.CUTOUT);
    }

    private static Block registerWithOutItem(String path, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props) {
        ResourceKey<Block> blockKey = ShapeShifterCurseFabric.blockKey(path);
        Block block = factory.apply(props.setId(blockKey));
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
        return block;
    }

    private static Block register(String path, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props) {
        ResourceKey<Block> blockKey = ShapeShifterCurseFabric.blockKey(path);
        Block block = factory.apply(props.setId(blockKey));
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
        ResourceKey<Item> itemKey = ResourceKey.create(BuiltInRegistries.ITEM.key(), ShapeShifterCurseFabric.identifier(path));
        Registry.register(BuiltInRegistries.ITEM, itemKey, new BlockItem(block, new Item.Properties().setId(itemKey)));
        return block;
    }

    private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(String path, BlockEntityType<T> blockEntityType) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ShapeShifterCurseFabric.identifier(path), blockEntityType);
    }

    public static void initialize() {
        // 蔓延速度=20, 燃烧速度=5，与木板相同
        FlammableBlockRegistry.getDefaultInstance().add(TEMP_WEB_BRIDGE, 60, 20);
    }
}

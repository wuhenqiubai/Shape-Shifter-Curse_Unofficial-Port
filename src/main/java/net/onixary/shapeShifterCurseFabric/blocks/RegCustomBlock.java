package net.onixary.shapeShifterCurseFabric.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
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

import java.util.function.Function;

public final class RegCustomBlock {
    public static final Block MOONDUST_CRYSTAL_GRIT = register("moondust_crystal_grit", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.GRAVEL).mapColor(MapColor.COLOR_PURPLE).strength(0.6f, 0.6f).sound(SoundType.GRAVEL));
    // TODO TEMP_WEB_BRIDGE 仅在测试时有物品 发布时记得用 registerWithOutItem
    public static final Block TEMP_WEB_BRIDGE = register("temp_web_bridge", TempWebBridgeBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(4.0f).randomTicks().noCollision().dynamicShape().noLootTable().isRedstoneConductor(Blocks::never).ignitedByLava().sound(SoundType.WOOL));

    public static final Block WEB_COMPOSTER = register("web_composter", WebComposterBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.6F).sound(SoundType.AZALEA).noOcclusion());
    public static final Block DEW_COVERED_COBWEB = register("dew_covered_cobweb", DewCoveredCobwebBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).instrument(NoteBlockInstrument.BELL).strength(1.0F).sound(SoundType.WOOL).noCollision().noOcclusion());

    // 合并 1.21.1：取「altar」改名（原 alter），但 API 保持 1.21.11 侧
    // （register 是工厂签名、FabricBlockEntityTypeBuilder、ChunkSectionLayer.CUTOUT）
    public static final Block Altar_BLOCK = register("altar", AltarBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).instrument(NoteBlockInstrument.BELL).strength(4.0F, 10.0F).sound(SoundType.AMETHYST).noOcclusion());
    public static final BlockEntityType<AltarBlockEntity> Altar_BLOCK_ENTITY = registerBlockEntity("altar_block_entity", FabricBlockEntityTypeBuilder.create(AltarBlockEntity::new, Altar_BLOCK).build());


    public static void ClientInit() {
        // 26.1 起无需手工声明方块渲染层：BakedQuad.MaterialInfo.of(...) 会用
        // ChunkSectionLayer.byTransparency(该 quad 所用 sprite 的透明度) 自动推导
        // （有中间 alpha → TRANSLUCENT；有全透明像素 → CUTOUT；全不透明 → SOLID），
        // Fabric 也据此删掉了 BlockRenderLayerMap。
        // 本模组这几张贴图都是二值 alpha（只有 0/255），自动推导结果正是原先手工指定的 CUTOUT；
        // web_composter 的 compost/ready 两张全不透明图还会各自得到 SOLID，比原来整块强制 CUTOUT 更准。
        // 方法保留为空以免改动调用方（ShapeShifterCurseFabricClient）。
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

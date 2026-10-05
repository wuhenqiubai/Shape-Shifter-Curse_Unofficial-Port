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
import net.onixary.shapeShifterCurseFabric.blocks.block_entity.FormAttunerBlockEntity;

import java.util.function.Function;

public final class RegCustomBlock {
    // 用 ofFullCopy 而非 of()：前者会连 gravel 的掉落物/爆炸抗性等一并继承，后者只给一份空白属性。
    // （合并上游时这行被改成了 of(Blocks.GRAVEL) —— 那是个不存在的重载 —— 后又降级成 of()，属性全丢。）
    public static final Block MOONDUST_CRYSTAL_GRIT = register("moondust_crystal_grit", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.GRAVEL).mapColor(MapColor.COLOR_PURPLE).strength(0.6f, 0.6f).sound(SoundType.GRAVEL));
    public static final Block TEMP_WEB_BRIDGE = register("temp_web_bridge", TempWebBridgeBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(4.0f).randomTicks().noCollision().dynamicShape().noLootTable().isRedstoneConductor(Blocks::never).ignitedByLava().sound(SoundType.WOOL));

    public static final Block WEB_COMPOSTER = register("web_composter", WebComposterBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.6F).sound(SoundType.AZALEA).noOcclusion());
    public static final Block DEW_COVERED_COBWEB = register("dew_covered_cobweb", DewCoveredCobwebBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).instrument(NoteBlockInstrument.BELL).strength(1.0F).sound(SoundType.WOOL).noCollision().noOcclusion());

    // 名称取 1.21.1 侧的 ALTAR_*（全项目其余文件都已用这个名字，ALTER_* 是拼写残留）；
    // 注册方式保留 1.21.11 侧的 Function + setId 重载（BlockItem 需要 ResourceKey 才能定描述前缀）。
    public static final Block ALTAR_BLOCK = register("altar", AltarBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).instrument(NoteBlockInstrument.BELL).strength(4.0F, 10.0F).sound(SoundType.AMETHYST).noOcclusion());
    public static final BlockEntityType<AltarBlockEntity> ALTAR_BLOCK_ENTITY = registerBlockEntity("altar_block_entity", FabricBlockEntityTypeBuilder.create(AltarBlockEntity::new, ALTAR_BLOCK).build(null));

    public static final Block FORM_ATTUNER_BLOCK = register("form_attuner", FormAttunerBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).instrument(NoteBlockInstrument.BELL).lightLevel((state) -> 15).strength(4.0F, 10.0F).sound(SoundType.GLASS).noOcclusion());
    public static final BlockEntityType<FormAttunerBlockEntity> FORM_ATTUNER_BLOCK_ENTITY = registerBlockEntity("form_attuner_block_entity", FabricBlockEntityTypeBuilder.create(FormAttunerBlockEntity::new, FORM_ATTUNER_BLOCK).build());

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
        // 1.21.11: BlockItem 不再覆写 getDescriptionId() 转发到方块（1.21.1 有），
        // 改由 Item.Properties 决定前缀。不显式声明的话方块物品的 key 会变成
        // "item.<ns>.<id>"，而 lang 里登记的是 "block.<ns>.<id>" → 物品栏显示原始 key。
        Registry.register(BuiltInRegistries.ITEM, itemKey, new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));
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

package net.onixary.shapeShifterCurseFabric.integration.origins.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;
import net.onixary.shapeShifterCurseFabric.integration.origins.content.TemporaryCobwebBlock;

import java.util.function.Function;

public class ModBlocks {

    // 1.21.11: Block 构造即需 Properties.id，静态初始化时通过工厂注册（setId 后构造）
    public static final Block TEMPORARY_COBWEB = registerBlock("temporary_cobweb", TemporaryCobwebBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).forceSolidOn().noCollision().requiresCorrectToolForDrops().strength(4.0F), false);

    private static Block registerBlock(String blockName, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props, boolean withBlockItem) {
        Identifier id = Identifier.fromNamespaceAndPath(Origins.MODID, blockName);
        ResourceKey<Block> blockKey = ResourceKey.create(BuiltInRegistries.BLOCK.key(), id);
        Block block = factory.apply(props.setId(blockKey));
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
        if(withBlockItem) {
            ResourceKey<Item> itemKey = ResourceKey.create(BuiltInRegistries.ITEM.key(), id);
            Registry.register(BuiltInRegistries.ITEM, itemKey, new BlockItem(block, new Item.Properties().setId(itemKey)));
        }
        return block;
    }

    /**
     * 仅用于**触发本类的静态初始化** —— 方块是在静态字段里调 registerBlock(...) 注册的，
     * 而 JVM 只在类被首次主动引用时才跑 {@code <clinit>}。必须由某个入口显式调用本方法，
     * 否则方块根本不会注册（表现为数据包 tag 报 "missing following references: origins:temporary_cobweb"）。
     * <p>
     * ⚠ 26.1 之前这个副作用是"顺带"发生的：{@code OriginsClient} 里有一行
     * {@code BlockRenderLayerMap.putBlock(ModBlocks.TEMPORARY_COBWEB, ...)} 引用了它。
     * 26.1 删除 BlockRenderLayerMap 时那行被移除，静态初始化就此失效 —— 故现在改为显式调用。
     */
    public static void register() {
        // 空实现：调用本身即触发 <clinit>
    }
}
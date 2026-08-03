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
}
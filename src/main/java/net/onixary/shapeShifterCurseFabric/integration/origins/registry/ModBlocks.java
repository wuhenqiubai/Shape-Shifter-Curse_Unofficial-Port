package net.onixary.shapeShifterCurseFabric.integration.origins.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;
import net.onixary.shapeShifterCurseFabric.integration.origins.content.TemporaryCobwebBlock;

public class ModBlocks {

    public static final Block TEMPORARY_COBWEB = new TemporaryCobwebBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).forceSolidOn().noCollision().requiresCorrectToolForDrops().strength(4.0F));

    public static void register() {
        register("temporary_cobweb", TEMPORARY_COBWEB, false);
    }

    private static void register(String blockName, Block block, boolean withBlockItem) {
        Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(Origins.MODID, blockName), block);
        if(withBlockItem) {
            Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(Origins.MODID, blockName), new BlockItem(block, new Item.Properties()));
        }
    }
}
package net.onixary.shapeShifterCurseFabric.integration.origins.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;
import net.onixary.shapeShifterCurseFabric.integration.origins.content.TemporaryCobwebBlock;

public class ModBlocks {

    private static final Identifier TEMPORARY_COBWEB_ID = Identifier.fromNamespaceAndPath(Origins.MODID, "temporary_cobweb");
    private static final ResourceKey<Block> TEMPORARY_COBWEB_KEY = ResourceKey.create(BuiltInRegistries.BLOCK.key(), TEMPORARY_COBWEB_ID);

    public static final Block TEMPORARY_COBWEB = new TemporaryCobwebBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).forceSolidOn().noCollision().requiresCorrectToolForDrops().strength(4.0F).setId(TEMPORARY_COBWEB_KEY));

    public static void register() {
        Registry.register(BuiltInRegistries.BLOCK, TEMPORARY_COBWEB_KEY, TEMPORARY_COBWEB);
    }
}

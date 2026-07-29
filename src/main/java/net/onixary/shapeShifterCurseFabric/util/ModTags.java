package net.onixary.shapeShifterCurseFabric.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

public class ModTags {
    public static final TagKey<EntityType<?>> Illager_Tag = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "illager"));
    public static final TagKey<EntityType<?>> Witch_Tag = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "witch"));
    public static final TagKey<EntityType<?>> Spider_Tag = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "spider"));
    public static final TagKey<Item> MorphScaleItem_Tag = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "morph_scale_item"));

    public static final TagKey<Item> Meat_Tag = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("origins", "meat"));
    public static final TagKey<Block> LIKE_SCAFFOLDING_TAG = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "like_scaffolding"));
    public static final TagKey<Block> LIKE_COBWEB_TAG = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "like_cobweb"));
    public static final TagKey<EntityType<?>> SPIDER_FLUID_COCOON_BLACKLIST = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "spider_fluid_cocoon_blacklist"));
}
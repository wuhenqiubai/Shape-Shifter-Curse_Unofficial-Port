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
    // 1.21.11 的 ToolMaterial 只接受 TagKey<Item> 作为修复材料（1.21.1 的 Tier 用的是 Ingredient，可直接写 Items.POWDER_SNOW_BUCKET）。
    // 雪花瓶在原版 1.21.1 是用 Items.POWDER_SNOW_BUCKET 修的，这里用等价的 mod tag 还原。
    public static final TagKey<Item> BOTTLED_SNOWFALL_TOOL_MATERIALS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "bottled_snowfall_tool_materials"));

    public static final TagKey<Item> Meat_Tag = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("origins", "meat"));
    public static final TagKey<Block> LIKE_SCAFFOLDING_TAG = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "like_scaffolding"));
    public static final TagKey<Block> LIKE_COBWEB_TAG = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "like_cobweb"));
    public static final TagKey<EntityType<?>> SPIDER_FLUID_COCOON_BLACKLIST = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "spider_fluid_cocoon_blacklist"));
}
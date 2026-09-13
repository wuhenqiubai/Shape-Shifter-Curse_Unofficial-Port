package net.onixary.shapeShifterCurseFabric.items.tools;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;
import net.onixary.shapeShifterCurseFabric.util.ModTags;

public class BottledSnowfallToolMaterial {
    // 顺序: incorrectBlocksForDrops / durability / speed / attackDamageBonus / enchantmentValue / repairItems
    // 修复材料沿用 1.21.1 的 Items.POWDER_SNOW_BUCKET —— 1.21.11 的 ToolMaterial 只收 TagKey<Item>，
    // 故落在 mod 自带的 shape-shifter-curse:bottled_snowfall_tool_materials 上（内容仍是雪花桶）。
    public static final ToolMaterial INSTANCE = new ToolMaterial(
        BlockTags.INCORRECT_FOR_WOODEN_TOOL,
        300,
        1f,
        0f,
        0,
        ModTags.BOTTLED_SNOWFALL_TOOL_MATERIALS
    );
}

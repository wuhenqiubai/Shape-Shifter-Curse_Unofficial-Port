package net.onixary.shapeShifterCurseFabric.items.tools;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;
import net.onixary.shapeShifterCurseFabric.util.ModTags;

public class BottledSnowfallToolMaterial {
    public static final ToolMaterial INSTANCE = new ToolMaterial(
        BlockTags.INCORRECT_FOR_WOODEN_TOOL,
        300,
        1f,
        0f,
        1,
        ModTags.BOTTLED_SNOWFALL_TOOL_MATERIALS
    );
}

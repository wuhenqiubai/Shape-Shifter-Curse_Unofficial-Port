package net.onixary.shapeShifterCurseFabric.items.tools;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ToolMaterial;

public class BottledSnowfallToolMaterial {
    public static final ToolMaterial INSTANCE = new ToolMaterial(
        BlockTags.INCORRECT_FOR_WOODEN_TOOL,
        300,
        1f,
        0f,
        0,
        ItemTags.STONE_TOOL_MATERIALS
    );
}

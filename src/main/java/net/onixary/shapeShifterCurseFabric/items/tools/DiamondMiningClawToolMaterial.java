package net.onixary.shapeShifterCurseFabric.items.tools;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ToolMaterial;

public class DiamondMiningClawToolMaterial {
    public static final ToolMaterial INSTANCE = new ToolMaterial(
        BlockTags.INCORRECT_FOR_WOODEN_TOOL,
        781,
        4f,
        2f,
        10,
        ItemTags.DIAMOND_TOOL_MATERIALS
    );
}

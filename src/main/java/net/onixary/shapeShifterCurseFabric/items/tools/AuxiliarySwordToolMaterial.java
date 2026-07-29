package net.onixary.shapeShifterCurseFabric.items.tools;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ToolMaterial;

public class AuxiliarySwordToolMaterial {
    public static final ToolMaterial INSTANCE = new ToolMaterial(
        BlockTags.INCORRECT_FOR_WOODEN_TOOL,
        781,
        1f,
        4f,
        0,
        ItemTags.DIAMOND_TOOL_MATERIALS
    );
}

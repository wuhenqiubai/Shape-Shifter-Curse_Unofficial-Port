package net.onixary.shapeShifterCurseFabric.items.tools;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ToolMaterial;

public class AuxiliaryAxeToolMaterial {
    // 顺序: incorrectBlocksForDrops / durability / speed / attackDamageBonus / enchantmentValue / repairItems
    // enchantmentValue 必须 > 0（1.21.11 的 Enchantable 会校验，0 会崩启动）；1.21.1 的 Tier 允许 0，故取最小值 1
    public static final ToolMaterial INSTANCE = new ToolMaterial(BlockTags.INCORRECT_FOR_WOODEN_TOOL,781,1f,6f,1,ItemTags.DIAMOND_TOOL_MATERIALS);
}

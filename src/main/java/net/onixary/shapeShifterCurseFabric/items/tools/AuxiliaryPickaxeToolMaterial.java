package net.onixary.shapeShifterCurseFabric.items.tools;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ToolMaterial;

public class AuxiliaryPickaxeToolMaterial {
    // enchantmentValue 必须 > 0：1.21.11 的 Enchantable record 会校验（"Enchantment value must be positive"），
    // 传 0 会在物品注册时抛 IllegalArgumentException 直接崩启动。1.21.1 的 Tier.getEnchantmentValue() 允许 0，
    // 高版本不允许，故取最小值 1（语义上等价于「不可附魔」）。其余四项同理。
    public static final ToolMaterial INSTANCE = new ToolMaterial(BlockTags.INCORRECT_FOR_WOODEN_TOOL,781,1.5f,2f,1,ItemTags.DIAMOND_TOOL_MATERIALS);
}
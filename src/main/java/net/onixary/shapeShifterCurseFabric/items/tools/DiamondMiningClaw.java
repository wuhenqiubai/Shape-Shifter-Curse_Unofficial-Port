package net.onixary.shapeShifterCurseFabric.items.tools;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

// 形态专属工具只应用耐久度逻辑，其他逻辑由形态Power+手持道具condition实现
public class DiamondMiningClaw extends Item {

    public DiamondMiningClaw(ToolMaterial material, int attackDamage, float attackSpeed, Properties settings) {
        super(settings.pickaxe(material, attackDamage, attackSpeed));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.translatable("item.shape-shifter-curse.diamond_mining_claw.tooltip").withStyle(ChatFormatting.YELLOW));
    }
}
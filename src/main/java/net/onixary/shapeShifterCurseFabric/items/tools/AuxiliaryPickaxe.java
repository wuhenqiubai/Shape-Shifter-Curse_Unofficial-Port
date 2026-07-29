package net.onixary.shapeShifterCurseFabric.items.tools;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;


public class AuxiliaryPickaxe extends Item {

    public AuxiliaryPickaxe(ToolMaterial material, int attackDamage, float attackSpeed, Properties settings) {
        super(settings.pickaxe(material, attackDamage, attackSpeed));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.translatable("item.shape-shifter-curse.auxiliary_pickaxe.tooltip").withStyle(ChatFormatting.YELLOW));
    }
}
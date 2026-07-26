package net.onixary.shapeShifterCurseFabric.items.tools;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

import java.util.List;


public class AuxiliaryPickaxe extends PickaxeItem {

    public AuxiliaryPickaxe(Tier material, int attackDamage, float attackSpeed, Properties settings) {
        super(material, settings);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.translatable("item.shape-shifter-curse.auxiliary_pickaxe.tooltip").withStyle(ChatFormatting.YELLOW));
    }
}
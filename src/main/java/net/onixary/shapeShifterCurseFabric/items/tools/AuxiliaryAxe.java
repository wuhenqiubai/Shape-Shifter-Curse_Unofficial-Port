package net.onixary.shapeShifterCurseFabric.items.tools;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;


public class AuxiliaryAxe extends AxeItem {

    public AuxiliaryAxe(ToolMaterial material, float attackDamage, float attackSpeed, Properties settings) {
        super(material, attackDamage, attackSpeed, settings);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag type) {
        consumer.accept(Component.translatable("item.shape-shifter-curse.auxiliary_axe.tooltip").withStyle(ChatFormatting.YELLOW));
    }
}
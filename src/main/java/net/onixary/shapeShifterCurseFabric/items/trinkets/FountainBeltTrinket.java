package net.onixary.shapeShifterCurseFabric.items.trinkets;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.onixary.shapeShifterCurseFabric.items.accessory.AccessoryItem;

import java.util.List;

public class FountainBeltTrinket extends AccessoryItem {
    public FountainBeltTrinket(Properties settings) {
        super(settings.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.translatable("item.shape-shifter-curse.fountain_belt.tooltip").withStyle(ChatFormatting.YELLOW));
    }
}

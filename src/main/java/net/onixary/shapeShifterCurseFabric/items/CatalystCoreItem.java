package net.onixary.shapeShifterCurseFabric.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class CatalystCoreItem extends Item {
    public CatalystCoreItem(Properties settings) {
        super(settings);
    }

    // 1.21.11：tooltip 参数由 List<Component> 变为 Consumer<Component>，并新增 TooltipDisplay。
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        tooltip.accept(Component.translatable(getDescriptionId() + ".tooltip").withStyle(ChatFormatting.YELLOW));
    }
}

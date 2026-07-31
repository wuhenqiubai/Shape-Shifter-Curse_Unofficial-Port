package net.onixary.shapeShifterCurseFabric.items.trinkets;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.onixary.shapeShifterCurseFabric.items.accessory.AccessoryItem;

import java.util.function.Consumer;

public class WitheredBandageTrinket extends AccessoryItem {
    public WitheredBandageTrinket(Properties settings) {
        super(settings.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag type) {
        consumer.accept(Component.translatable("item.shape-shifter-curse.withered_bandage.tooltip").withStyle(ChatFormatting.YELLOW));
    }
}

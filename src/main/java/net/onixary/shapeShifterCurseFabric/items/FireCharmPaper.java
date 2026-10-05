package net.onixary.shapeShifterCurseFabric.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class FireCharmPaper extends Item {
    public FireCharmPaper(Properties settings) {
        super(settings.stacksTo(64));
    }

    @Override
    // 1.21.11: appendHoverText 形参改为 (ItemStack, TooltipContext, TooltipDisplay, Consumer<Component>, TooltipFlag)，
    // 取 1.21.1 侧改用 getDescriptionId() 拼 tooltip 键的写法（等价于原来的硬编码键，减少后续改名遗漏）。
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag type) {
        consumer.accept(Component.translatable(getDescriptionId() + ".tooltip").withStyle(ChatFormatting.YELLOW));
    }
}

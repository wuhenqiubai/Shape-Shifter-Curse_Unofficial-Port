package net.onixary.shapeShifterCurseFabric.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class SpiderFluidCocoon extends Item {
    public SpiderFluidCocoon(Properties settings) {
        super(settings
                .stacksTo(64)
                .food(
                        new FoodProperties.Builder()
                                .nutrition(6)
                                .saturationModifier(0.8f)
                                .build(),
                        // 1.21.11 起 Item.getEatingSound() 被移除，食用音效挪进了数据组件 Consumable。
                        // 1.21.1 的写法是覆写 getEatingSound() 返回 GENERIC_DRINK（其余参数沿用默认食物），
                        // 这里用「defaultFood() + 换饮用音效」等价还原。
                        Consumables.defaultFood().sound(SoundEvents.GENERIC_DRINK).build()
                ));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag type) {
        consumer.accept(Component.translatable("item.shape-shifter-curse.spider_fluid_cocoon.tooltip").withStyle(ChatFormatting.YELLOW));
    }
}
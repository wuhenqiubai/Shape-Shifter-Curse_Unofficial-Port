package net.onixary.shapeShifterCurseFabric.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class SpiderFluidCocoon extends Item {
    public SpiderFluidCocoon(Properties settings) {
        super(settings
                .stacksTo(64)
                .food(
                        new FoodProperties.Builder()
                                .nutrition(6)
                                .saturationModifier(0.8f)
                                .effect(new MobEffectInstance(MobEffects.POISON, 150, 0), 1.0f)
                                .build()
                ));
    }

    @Override
    public SoundEvent getEatingSound(){
        return SoundEvents.GENERIC_DRINK;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.translatable("item.shape-shifter-curse.spider_fluid_cocoon.tooltip").withStyle(ChatFormatting.YELLOW));
    }
}
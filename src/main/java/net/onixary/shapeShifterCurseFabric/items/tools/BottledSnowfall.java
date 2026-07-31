package net.onixary.shapeShifterCurseFabric.items.tools;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

// 形态专属工具只应用耐久度逻辑，其他逻辑由形态Power+手持道具condition实现
public class BottledSnowfall extends Item {

    public BottledSnowfall(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Properties settings) {
        super(settings.sword(toolMaterial, attackDamage, attackSpeed));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag type) {
        consumer.accept(Component.translatable("item.shape-shifter-curse.bottled_snowfall.tooltip").withStyle(ChatFormatting.YELLOW));
    }
}
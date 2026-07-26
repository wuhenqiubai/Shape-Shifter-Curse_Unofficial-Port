package net.onixary.shapeShifterCurseFabric.items.tools;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

import java.util.List;

// 形态专属工具只应用耐久度逻辑，其他逻辑由形态Power+手持道具condition实现
public class BottledSnowfall extends SwordItem {

    public BottledSnowfall(Tier toolMaterial, int attackDamage, float attackSpeed, Properties settings) {
        super(toolMaterial, settings);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.translatable("item.shape-shifter-curse.bottled_snowfall.tooltip").withStyle(ChatFormatting.YELLOW));
    }
}
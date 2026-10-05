package net.onixary.shapeShifterCurseFabric.items.trinkets;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.onixary.shapeShifterCurseFabric.items.accessory.AccessoryItem;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/** An independent accessory whose form powers are supplied by accessory_power data. */
public class FormTrinket extends AccessoryItem {
    public FormTrinket(Properties properties) {
        super(properties.stacksTo(1));
    }

    // 1.21.11：tooltip 参数由 List<Component> 变为 Consumer<Component>，并新增 TooltipDisplay。
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext world, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag context) {
        tooltip.accept(Component.translatable(getDescriptionId() + ".tooltip").withStyle(ChatFormatting.YELLOW));
    }
}

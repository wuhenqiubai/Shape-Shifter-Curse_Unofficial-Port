package net.onixary.shapeShifterCurseFabric.studio;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.onixary.shapeShifterCurseFabric.items.accessory.AccessoryItem;
import net.onixary.shapeShifterCurseFabric.player_form.utils.PlayerFormComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Consumer;

/** Generic registered accessory; power callbacks are supplied by the existing accessory mixin. */
public final class StudioAccessory extends AccessoryItem {
    private final String description;
    private final Set<Identifier> allowedForms;

    public StudioAccessory(Properties properties, String description, Set<Identifier> allowedForms) {
        super(properties);
        this.description = description;
        this.allowedForms = Set.copyOf(allowedForms);
    }

    @Override
    public boolean canEquip(ItemStack stack, LivingEntity entity, SlotData slot) {
        return allowedForms.isEmpty() || (entity instanceof Player player &&
                allowedForms.contains(PlayerFormComponent.COMPONENT.get(player).nowFormID));
    }

    // 1.21.11：tooltip 参数由 List<Component> 变为 Consumer<Component>，并新增 TooltipDisplay。
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext world, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag context) {
        if (!description.isEmpty()) tooltip.accept(Component.literal(description));
    }
}

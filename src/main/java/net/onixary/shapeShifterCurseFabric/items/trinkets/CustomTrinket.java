package net.onixary.shapeShifterCurseFabric.items.trinkets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.items.accessory.AccessoryItem;
import net.onixary.shapeShifterCurseFabric.util.TrinketUtils;
import org.jetbrains.annotations.NotNull;

public class CustomTrinket extends AccessoryItem implements TrinketUtils.CustomPowerTrinketInterface {
    static {
        TrinketUtils.registerAccessoryMixinAuto(ShapeShifterCurseFabric.identifier("custom_trinket"), false);
    }
    private static final Identifier DEFAULT_IDENTIFIER = ShapeShifterCurseFabric.identifier("custom_trinket");

    public CustomTrinket(Properties settings) {
        super(settings);
    }

    private @NotNull Identifier getAccessoryID(ItemStack stack) {
        // getNbt() removed in 1.21; use component-based access
        var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return DEFAULT_IDENTIFIER;
        }
        CompoundTag nbt = customData.copyTag();
        if (nbt.contains("custom_accessory_id")) {
            Identifier identifier = Identifier.tryParse(nbt.getString("custom_accessory_id"));
            if (identifier != null) {
                return identifier;
            }
        }
        return DEFAULT_IDENTIFIER;
    }

    @Override
    public boolean canEquip(ItemStack stack, LivingEntity entity, AccessoryItem.SlotData slot) {
        var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return false;
        }
        CompoundTag nbt = customData.copyTag();
        if (nbt.contains("custom_accessory_slots")) {
            ListTag slots = nbt.getList("custom_accessory_slots", 8);
            Identifier slotFinalName = slot.slot();
            for (int i = 0; i < slots.size(); i++) {
                if (slots.getString(i).equals(slotFinalName.toString())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onEquip(ItemStack stack, LivingEntity entity, AccessoryItem.SlotData slot) {
        if (entity instanceof Player player) {
            TrinketUtils.ApplyAccessoryPowerOnEquip(player, getAccessoryID(stack));
        }
    }

    @Override
    public void onUnequip(ItemStack stack, LivingEntity entity, AccessoryItem.SlotData slot) {
        if (entity instanceof Player player) {
            TrinketUtils.ApplyAccessoryPowerOnUnEquip(player, getAccessoryID(stack));
        }
    }

    @Override
    public void onFormChange(ItemStack stack, AccessoryItem.SlotData slot, Player player) {
        TrinketUtils.ApplyAccessoryPowerOnPlayerFormChange(player, getAccessoryID(stack));
    }
}
package io.github.apace100.apoli.util;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;

public interface PowerGrantingItem {

    Collection<StackPowerUtil.StackPower> getPowers(ItemStack stack, EquipmentSlot slot);
}

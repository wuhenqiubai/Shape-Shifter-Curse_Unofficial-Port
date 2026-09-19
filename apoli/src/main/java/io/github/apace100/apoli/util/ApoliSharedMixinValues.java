package io.github.apace100.apoli.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;

public interface ApoliSharedMixinValues {
    ThreadLocal<GrindstoneMenu> CURRENT_GRINDSTONE_MENU = new ThreadLocal<>();
    ThreadLocal<ItemStack> CURRENT_STACK = new ThreadLocal<>();
    ThreadLocal<LivingEntity> CURRENT_ENTITY = new ThreadLocal<>();
}

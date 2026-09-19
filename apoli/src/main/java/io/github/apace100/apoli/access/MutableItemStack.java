package io.github.apace100.apoli.access;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface MutableItemStack {

    void setItem(Item item);

    void setFrom(ItemStack stack);
}

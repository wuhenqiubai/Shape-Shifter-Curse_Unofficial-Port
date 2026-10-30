package net.onixary.shapeShifterCurseFabric.items.accessory;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;

public abstract class AccessoryItem extends Item {

	public record SlotData(Identifier slot, int index) { }

	public enum DropRule {
		KEEP,
		DROP,
		DESTROY,
		DEFAULT
	}

    public AccessoryItem(Properties settings) {
	    super(settings);
	    this.accessoryInit(settings);
    }

    public void accessoryInit(Properties settings) {
    }

    public void accessoryTick(ItemStack stack, LivingEntity accessoryOwner, SlotData slotData) {
    }

    public void onEquip(ItemStack stack, LivingEntity accessoryOwner, SlotData slotData) {
    }

    public void onUnequip(ItemStack stack, LivingEntity accessoryOwner, SlotData slotData) {
    }

    public boolean canEquip(ItemStack stack, LivingEntity entity, SlotData slotData) {
        return true;
    }

    public boolean canUnequip(ItemStack stack, LivingEntity entity, SlotData slotData) {
        return stack.getEnchantments().keySet().stream()
                .noneMatch(holder -> holder.is(Enchantments.BINDING_CURSE));
    }

    public void onBreak(ItemStack stack, LivingEntity entity, SlotData slotData) {
    }

    public DropRule getDropRule(ItemStack stack, LivingEntity entity, SlotData slotData) {
        return DropRule.DEFAULT;
    }
}
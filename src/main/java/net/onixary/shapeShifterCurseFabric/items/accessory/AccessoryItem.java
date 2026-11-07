package net.onixary.shapeShifterCurseFabric.items.accessory;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public abstract class AccessoryItem extends Item {

	public record SlotData(ResourceLocation slot, int index) { }

	public static SlotData slotDataOf(String ioName, @Nullable String group, @Nullable String slotName, int index) {
		String namespace = ioName;
		String path = slotName == null ? "" : slotName;
		int colonIndex = path.indexOf(':');
		if (colonIndex > 0 && colonIndex < path.length() - 1) {
			namespace = path.substring(0, colonIndex);
			path = path.substring(colonIndex + 1);
		} else if (group != null && !group.isEmpty()) {
			path = "%s/%s".formatted(group, path);
		}
		ResourceLocation parsed = ResourceLocation.tryParse(namespace + ":" + path);
		if (parsed != null) {
			return new SlotData(parsed, index);
		}
		ResourceLocation sanitized = ResourceLocation.tryParse(sanitize(namespace, false) + ":" + sanitize(path, true));
		if (sanitized != null) {
			return new SlotData(sanitized, index);
		}
		return new SlotData(ResourceLocation.fromNamespaceAndPath("shape-shifter-curse", "unknown_slot"), index);
	}

	private static String sanitize(String raw, boolean allowSlash) {
		String cleaned = raw.toLowerCase(Locale.ROOT).replaceAll(allowSlash ? "[^a-z0-9/._-]" : "[^a-z0-9._-]", "_");
		return cleaned.isEmpty() ? "unknown" : cleaned;
	}

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
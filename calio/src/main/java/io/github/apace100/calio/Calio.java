package io.github.apace100.calio;

import com.mojang.serialization.Codec;
import io.github.apace100.calio.network.CalioNetworking;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

public class Calio implements ModInitializer {
	public static final DataComponentType<Boolean> NON_ITALIC_NAME = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, ResourceLocation.fromNamespaceAndPath("calio", "non_italic_name"), DataComponentType.<Boolean>builder()
		.persistent(Codec.BOOL)
		.networkSynchronized(ByteBufCodecs.BOOL)
		.build()
	);

	public static final DataComponentType<Boolean> HAS_ADDITIONAL_ATTRIBUTES = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, ResourceLocation.fromNamespaceAndPath("calio", "has_additional_attributes"), DataComponentType.<Boolean>builder()
		.persistent(Codec.BOOL)
		.networkSynchronized(ByteBufCodecs.BOOL)
		.build()
	);

	@Override
	public void onInitialize() {
		CalioNetworking.init();
		Registry.register(BuiltInRegistries.TRIGGER_TYPES, ResourceLocation.fromNamespaceAndPath("calio", "code_trigger"), CodeTriggerCriterion.INSTANCE);
	}

	public static boolean hasNonItalicName(ItemStack stack) {
		return stack.getOrDefault(NON_ITALIC_NAME, false);
	}

	public static void setNameNonItalic(ItemStack stack) {
		if(stack != null)
			stack.set(NON_ITALIC_NAME, true);
	}

	public static boolean areEntityAttributesAdditional(ItemStack stack) {
		return stack.getOrDefault(HAS_ADDITIONAL_ATTRIBUTES, false);
	}

	/**
	 * Sets whether the item stack counts the entity attribute modifiers specified in its tag as additional,
	 * meaning they won't overwrite the equipment's inherent modifiers.
	 * @param stack
	 * @param additional
	 */
	public static void setEntityAttributesAdditional(ItemStack stack, boolean additional) {
		if(stack != null) {
			if(additional) {
				stack.set(HAS_ADDITIONAL_ATTRIBUTES, true);
			} else {
				stack.remove(HAS_ADDITIONAL_ATTRIBUTES);
			}
		}
	}

	public static <T> boolean areTagsEqual(ResourceKey<? extends Registry<T>> registryKey, TagKey<T> tag1, TagKey<T> tag2) {
		return areTagsEqual(tag1, tag2);
	}

	public static <T> boolean areTagsEqual(TagKey<T> tag1, TagKey<T> tag2) {
		if(tag1 == tag2) {
			return true;
		}
		if(tag1 == null || tag2 == null) {
			return false;
		}
		if(!tag1.registry().equals(tag2.registry())) {
			return false;
		}
		if(!tag1.location().equals(tag2.location())) {
			return false;
		}
		return true;
	}
}

package io.github.apace100.calio.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class CalioTags {
    public static TagKey<EntityType<?>> DEFAULT_ENTITY_TYPE = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("calio", "default"));
}

package io.github.apace100.apoli.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.PowerTypes;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ApoliResourceConditions {

	public static final ResourceConditionType<NamespacesLoadedResourceCondition> ANY_NAMESPACE_LOADED = ResourceConditionType.create(Apoli.identifier("any_namespace_loaded"), NamespacesLoadedResourceCondition.ANY_CODEC);

	public static final ResourceConditionType<NamespacesLoadedResourceCondition> ALL_NAMESPACES_LOADED = ResourceConditionType.create(Apoli.identifier("all_namespaces_loaded"), NamespacesLoadedResourceCondition.ALL_CODEC);

	public static class NamespacesLoadedResourceCondition implements ResourceCondition {
		public static final MapCodec<NamespacesLoadedResourceCondition> ANY_CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(
				Codec.STRING.listOf()
					.fieldOf("namespaces")
					.forGetter(e -> e.namespaces)
			)
				.apply(instance, namespaces -> new NamespacesLoadedResourceCondition(namespaces, false))
		);

		public static final MapCodec<NamespacesLoadedResourceCondition> ALL_CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(
					Codec.STRING.listOf()
						.fieldOf("namespaces")
						.forGetter(e -> e.namespaces)
				)
				.apply(instance, namespaces -> new NamespacesLoadedResourceCondition(namespaces, true))
		);

		private final List<String> namespaces;
		private final boolean and;

		public NamespacesLoadedResourceCondition(List<String> namespaces, boolean and) {
			this.namespaces = namespaces;
			this.and = and;
		}

		@Override
		public ResourceConditionType<?> getType() {
			if (and)
				return ALL_NAMESPACES_LOADED;
			else
				return ANY_NAMESPACE_LOADED;
		}

		@Override
		public boolean test(RegistryOps.@Nullable RegistryInfoLookup registryInfo) {
			for (String namespace : namespaces) {
				if (PowerTypes.LOADED_NAMESPACES.contains(namespace) != and) {
					return !and;
				}
			}

			return and;
		}
	}

	public static boolean namespacesLoaded(JsonObject jsonObject, Set<String> namespaces, boolean and) {

		JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "namespaces");
		for (JsonElement jsonElement : jsonArray) {
			if (jsonElement.isJsonPrimitive()) {
				if (namespaces.contains(jsonElement.getAsString()) != and) {
					return !and;
				}
			} else {
				throw new JsonParseException("Invalid " + jsonElement + " entry: expected a JSON string!");
			}
		}

		return and;

	}

	public static boolean test(Identifier id, JsonObject jsonObject) {

		try {
			JsonArray conditions = GsonHelper.getAsJsonArray(jsonObject, ResourceConditions.CONDITIONS_KEY, null);
			if (conditions == null) {
				return true;
			} else {
				List<ResourceCondition> parsedConditions = new ArrayList<>();

				for (JsonElement condition : conditions) {
					var json = condition.getAsJsonObject();
					var conditionId = Identifier.parse(json.get("condition").getAsString());
					parsedConditions.add(ResourceConditions.getConditionType(conditionId).codec().codec().decode(JsonOps.INSTANCE, json).getOrThrow().getFirst());
				}

				return ResourceConditionsImpl.conditionsMet(parsedConditions, null, true);
			}
		} catch (RuntimeException e) {
			Apoli.LOGGER.error("There was a problem parsing the resource condition(s) of power file " + id + " (skipping): " + e.getMessage());
			return false;
		}

	}

}

package io.github.apace100.apoli;

import de.dafuqs.additionalentityattributes.AdditionalEntityAttributes;
import io.github.apace100.apoli.command.PowerCommand;
import io.github.apace100.apoli.command.ResourceCommand;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.component.PowerHolderComponentImpl;
import io.github.apace100.apoli.component.StackPowerComponent;
import io.github.apace100.apoli.global.GlobalPowerSetLoader;
import io.github.apace100.apoli.networking.ModPackets;
import io.github.apace100.apoli.networking.ModPacketsC2S;
import io.github.apace100.apoli.power.PowerTypes;
import io.github.apace100.apoli.power.factory.PowerFactories;
import io.github.apace100.apoli.power.factory.action.BiEntityActions;
import io.github.apace100.apoli.power.factory.action.BlockActions;
import io.github.apace100.apoli.power.factory.action.EntityActions;
import io.github.apace100.apoli.power.factory.action.ItemActions;
import io.github.apace100.apoli.power.factory.condition.*;
import io.github.apace100.apoli.registry.ApoliClassData;
import io.github.apace100.apoli.util.*;
import io.github.apace100.apoli.util.modifier.ModifierOperations;
import io.github.apace100.calio.resource.OrderedResourceListenerInitializer;
import io.github.apace100.calio.resource.OrderedResourceListenerManager;
import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.Pal;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

import java.util.List;

public class Apoli implements ModInitializer, EntityComponentInitializer, OrderedResourceListenerInitializer {

	public static ApoliConfig config;

	public static MinecraftServer server;

	public static final Scheduler SCHEDULER = new Scheduler();

	public static final String MODID = "apoli";
	public static final String LEGACY_MODID = "apoli_legacy";

	public static final Logger LOGGER = LogManager.getLogger(Apoli.class);
	public static String VERSION = "";
	public static int[] SEMVER;

	public static final AbilitySource LEGACY_POWER_SOURCE = Pal.getAbilitySource(Apoli.identifier("power_source"));

	public static final boolean PERFORM_VERSION_CHECK = false;

	@Override
	public void onInitialize() {
		ModPackets.init();
		ServerLifecycleEvents.SERVER_STARTED.register(s -> server = s);

		FabricLoader.getInstance().getModContainer(MODID).ifPresent(modContainer -> {
			VERSION = modContainer.getMetadata().getVersion().getFriendlyString();
			if(VERSION.contains("+")) {
				VERSION = VERSION.split("\\+")[0];
			}
			if(VERSION.contains("-")) {
				VERSION = VERSION.split("-")[0];
			}
			String[] splitVersion = VERSION.split("\\.");
			SEMVER = new int[splitVersion.length];
			for(int i = 0; i < SEMVER.length; i++) {
				SEMVER[i] = Integer.parseInt(splitVersion[i]);
			}
		});

		ModPacketsC2S.register();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			PowerCommand.register(dispatcher);
			ResourceCommand.register(dispatcher);
		});

		Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Apoli.identifier("power_restricted"), PowerRestrictedCraftingRecipe.SERIALIZER);
		Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Apoli.identifier("modified"), ModifiedCraftingRecipe.SERIALIZER);
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Apoli.identifier("stack_powers"), StackPowerComponent.TYPE);

		Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, Apoli.identifier("add_power"), AddPowerLootFunction.CODEC);
		Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, Apoli.identifier("remove_power"), RemovePowerLootFunction.CODEC);

		Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, Apoli.identifier("power"), PowerLootCondition.CODEC);

		ApoliClassData.registerAll();

		ModifierOperations.registerAll();

		PowerFactories.register();
		EntityConditions.register();
		BiEntityConditions.register();
		ItemConditions.register();
		BlockConditions.register();
		DamageConditions.register();
		FluidConditions.register();
		BiomeConditions.register();
		EntityActions.register();
		ItemActions.register();
		BlockActions.register();
		BiEntityActions.register();

		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new GlobalPowerSetLoader());
		ResourceConditions.register(ApoliResourceConditions.ANY_NAMESPACE_LOADED);
		ResourceConditions.register(ApoliResourceConditions.ALL_NAMESPACES_LOADED);

		Registry.register(BuiltInRegistries.TRIGGER_TYPES, Apoli.identifier("gained_power"), GainedPowerCriterion.INSTANCE);

		LOGGER.info("Apoli " + VERSION + " has initialized. Ready to power up your game!");

		// Legacy: Updating the world can sometimes encounter chunk bans caused by Additional Entity Attributes. Which is weird, considering it should be getting datafixed automatically.
		var genericAttributes = List.of(
			AdditionalEntityAttributes.CRITICAL_BONUS_DAMAGE,
			AdditionalEntityAttributes.WATER_SPEED,
			AdditionalEntityAttributes.LAVA_SPEED,
			AdditionalEntityAttributes.WIDTH,
			AdditionalEntityAttributes.HEIGHT,
			AdditionalEntityAttributes.HITBOX_SCALE,
			AdditionalEntityAttributes.HITBOX_WIDTH,
			AdditionalEntityAttributes.HITBOX_HEIGHT,
			AdditionalEntityAttributes.MODEL_SCALE,
			AdditionalEntityAttributes.MODEL_HEIGHT,
			AdditionalEntityAttributes.MODEL_WIDTH,
			AdditionalEntityAttributes.MOB_DETECTION_RANGE,
			AdditionalEntityAttributes.MAGIC_PROTECTION
		);

		var playerAttributes = List.of(
			AdditionalEntityAttributes.WATER_VISIBILITY,
			AdditionalEntityAttributes.LAVA_VISIBILITY,
			AdditionalEntityAttributes.BONUS_LOOT_COUNT_ROLLS,
			AdditionalEntityAttributes.BONUS_RARE_LOOT_ROLLS,
			AdditionalEntityAttributes.DROPPED_EXPERIENCE,
			AdditionalEntityAttributes.COLLECTION_RANGE
		);

		for (Holder<Attribute> attribute : genericAttributes) {
			var id = attribute.unwrapKey().orElseThrow().identifier();
			BuiltInRegistries.ATTRIBUTE.addAlias(id.withPrefix("generic."), id);
		}

		for (Holder<Attribute> attribute : playerAttributes) {
			var id = attribute.unwrapKey().orElseThrow().identifier();
			BuiltInRegistries.ATTRIBUTE.addAlias(id.withPrefix("player."), id);
		}

		ResourceLoader loader = ResourceLoader.get(PackType.SERVER_DATA);
		loader.registerReloadListener(identifier("powers"), new PowerTypes());
		loader.addListenerOrdering(ResourceReloaderKeys.AFTER_VANILLA, identifier("powers"));
	}

	public static Identifier identifier(String path) {
		return Identifier.fromNamespaceAndPath(MODID, path);
	}

	public static Identifier legacy(String path) {
		return Identifier.fromNamespaceAndPath(LEGACY_MODID, path);
	}

	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
		registry.beginRegistration(LivingEntity.class, PowerHolderComponent.KEY)
			.impl(PowerHolderComponentImpl.class)
			.respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY)
			.end(PowerHolderComponentImpl::new);
	}

	@Override
	public void registerResourceListeners(OrderedResourceListenerManager manager) {
	}
}

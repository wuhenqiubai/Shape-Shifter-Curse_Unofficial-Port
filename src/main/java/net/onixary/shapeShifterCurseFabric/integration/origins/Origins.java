package net.onixary.shapeShifterCurseFabric.integration.origins;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypes;
import io.github.apace100.calio.resource.OrderedResourceListenerInitializer;
import io.github.apace100.calio.resource.OrderedResourceListenerManager;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.ConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.CreativeModeTabs;
import net.onixary.shapeShifterCurseFabric.integration.origins.badge.BadgeManager;
import net.onixary.shapeShifterCurseFabric.integration.origins.networking.ModPackets;
import net.onixary.shapeShifterCurseFabric.integration.origins.networking.ModPacketsC2S;
import net.onixary.shapeShifterCurseFabric.networking.BytePayload;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.Origin;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.OriginLayers;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.OriginManager;
import net.onixary.shapeShifterCurseFabric.integration.origins.power.OriginsEntityConditions;
import net.onixary.shapeShifterCurseFabric.integration.origins.power.OriginsPowerTypes;
import net.onixary.shapeShifterCurseFabric.integration.origins.registry.*;
import net.onixary.shapeShifterCurseFabric.integration.origins.util.ChoseOriginCriterion;
import net.onixary.shapeShifterCurseFabric.integration.origins.util.OriginsConfigSerializer;
import net.onixary.shapeShifterCurseFabric.integration.origins.util.OriginsJsonConfigSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Origins implements ModInitializer, OrderedResourceListenerInitializer {

	public static final String MODID = "origins";
	public static String VERSION = "";
	public static int[] SEMVER;
	public static final Logger LOGGER = LogManager.getLogger(Origins.class);

	public static ServerConfig config;
	private static ConfigSerializer<ServerConfig> configSerializer;

	@Override
	public void onInitialize() {
		FabricLoader.getInstance().getModContainer("shape-shifter-curse-unofficial").ifPresent(modContainer -> {
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
		LOGGER.info("Origins " + VERSION + " is initializing. Have fun!");

		AutoConfig.register(ServerConfig.class,
			(definition, configClass) -> {
				configSerializer = new OriginsJsonConfigSerializer<>(definition, configClass,
					new OriginsConfigSerializer<>(definition, configClass));
				return configSerializer;
			});
		config = AutoConfig.getConfigHolder(ServerConfig.class).getConfig();

		OriginsPowerTypes.register();
		OriginsEntityConditions.register();

		ModBlocks.register();
		ModItems.register();
		ModTags.register();
		ModPacketsC2S.register();
		// 服务端也要注册 S2C payload 类型（发送端必须注册 playS2C，否则 id 未知回退 DiscardedPayload 编码失败，进服即崩 -- issue #21）
		BytePayload.registerS2C(ModPackets.ORIGIN_LIST);
		BytePayload.registerS2C(ModPackets.LAYER_LIST);
		BytePayload.registerS2C(ModPackets.OPEN_ORIGIN_SCREEN);
		BytePayload.registerS2C(ModPackets.CONFIRM_ORIGIN);
		BytePayload.registerS2C(ModPackets.BADGE_LIST);
		ModEnchantments.register();
		ModEntities.register();
		ModLoot.registerLootTables();
		Origin.init();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			//OriginCommand.register(dispatcher);
		});
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register((content) -> {
			//content.add(ModItems.ORB_OF_ORIGIN);
		});

		net.minecraft.advancements.CriteriaTriggers.register(ChoseOriginCriterion.ID.toString(), ChoseOriginCriterion.INSTANCE);
	}

	public static void serializeConfig() {
		try {
			configSerializer.serialize(config);
		} catch (ConfigSerializer.SerializationException e) {
			Origins.LOGGER.error("Failed serialization of config file: " + e.getMessage());
		}
	}

	public static ResourceLocation identifier(String path) {
		return ResourceLocation.fromNamespaceAndPath(Origins.MODID, path);
	}

	@Override
	public void registerResourceListeners(OrderedResourceListenerManager manager) {
		ResourceLocation powerData = Apoli.identifier("powers");
		ResourceLocation originData = Origins.identifier("origins");

		// Ensure origins namespace tags are registered before power loading
		OriginsTagLoader tagLoader = new OriginsTagLoader();
		manager.register(PackType.SERVER_DATA, tagLoader).before(powerData).complete();
		PowerTypes.DEPENDENCIES.add(tagLoader.getFabricId());

		// 1.21.11: 用 registerWithRegistries 传入 HolderLookup.Provider（Origin 解析 icon 的 ItemStack 需要非 null registry）
		manager.registerWithRegistries(originData, OriginManager::new).after(powerData).complete();
		manager.register(PackType.SERVER_DATA, new OriginLayers()).after(originData).complete();

		BadgeManager.init();

		IdentifiableResourceReloadListener badgeLoader = BadgeManager.REGISTRY.getLoader();
		manager.register(PackType.SERVER_DATA, badgeLoader).before(powerData).complete();
		PowerTypes.DEPENDENCIES.add(badgeLoader.getFabricId());
	}

	@Config(name = Origins.MODID + "_server")
	public static class ServerConfig implements ConfigData {

		public boolean performVersionCheck = true;

		public JsonObject origins = new JsonObject();

		public boolean isOriginDisabled(ResourceLocation originId) {
			String idString = originId.toString();
			if(!origins.has(idString)) {
				return false;
			}
			JsonElement element = origins.get(idString);
			if(element instanceof JsonObject jsonObject) {
				return !GsonHelper.getAsBoolean(jsonObject, "enabled", true);
			}
			return false;
		}

		public boolean isPowerDisabled(ResourceLocation originId, ResourceLocation powerId) {
			String originIdString = originId.toString();
			if(!origins.has(originIdString)) {
				return false;
			}
			String powerIdString = powerId.toString();
			JsonElement element = origins.get(originIdString);
			if(element instanceof JsonObject jsonObject) {
				return !GsonHelper.getAsBoolean(jsonObject, powerIdString, true);
			}
			return false;
		}

		public boolean addToConfig(Origin origin) {
			boolean changed = false;
			String originIdString = origin.getIdentifier().toString();
			JsonObject originObj;
			if(!origins.has(originIdString) || !(origins.get(originIdString) instanceof JsonObject)) {
				originObj = new JsonObject();
				origins.add(originIdString, originObj);
				changed = true;
			} else {
				originObj = (JsonObject) origins.get(originIdString);
			}
			if(!originObj.has("enabled") || !(originObj.get("enabled") instanceof JsonPrimitive)) {
				originObj.addProperty("enabled", Boolean.TRUE);
				changed = true;
			}
			for(PowerType<?> power : origin.getPowerTypes()) {
				String powerIdString = power.getIdentifier().toString();
				if(!originObj.has(powerIdString) || !(originObj.get(powerIdString) instanceof JsonPrimitive)) {
					originObj.addProperty(powerIdString, Boolean.TRUE);
					changed = true;
				}
			}
			return changed;
		}
	}
}
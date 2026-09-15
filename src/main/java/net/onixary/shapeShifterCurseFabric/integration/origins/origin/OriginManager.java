package net.onixary.shapeShifterCurseFabric.integration.origins.origin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.calio.data.MultiJsonDataLoader;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class OriginManager extends MultiJsonDataLoader implements PreparableReloadListener {

	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

	// 26.1: 改走 Fabric v1 ResourceLoader 后，不再由 Calio 的 registerWithRegistries 注入 provider，
	// 改为在 prepareSharedState 里从 shared state 取（与 Apoli 的 PowerTypes 同款做法）。
	// Origin.fromJson(...) 用它解析 icon 的 ItemStack，为 null 会 NPE —— 故 apply 处有显式检查。
	private @Nullable HolderLookup.Provider provider;

	public OriginManager() {
		super(GSON, "origins");
	}

	@Override
	public void prepareSharedState(SharedState currentReload) {
		super.prepareSharedState(currentReload);
		this.provider = currentReload.get(ResourceLoader.REGISTRY_LOOKUP_KEY);
	}

	@Override
	protected void apply(Map<Identifier, List<JsonElement>> loader, ResourceManager manager, ProfilerFiller profiler) {
		if (this.provider == null) {
			// 正常不会发生（prepareSharedState 先于 apply）。真出现时给出可诊断的报错，
			// 而不是让 Origin.fromJson 解析 icon 时抛一个难定位的 NPE。
			Origins.LOGGER.error("OriginManager: registry lookup 未就绪（prepareSharedState 没从 ResourceLoader.REGISTRY_LOOKUP_KEY 取到）—— 本次跳过 origin 加载");
			return;
		}
		OriginRegistry.reset();
		AtomicBoolean hasConfigChanged = new AtomicBoolean(false);
		loader.forEach((id, jel) -> {
			jel.forEach(je -> {
				try {
					Origin origin = Origin.fromJson(id, je.getAsJsonObject(), provider);
					if(!OriginRegistry.contains(id)) {
						OriginRegistry.register(id, origin);
					} else {
						if(OriginRegistry.get(id).getLoadingPriority() < origin.getLoadingPriority()) {
							OriginRegistry.update(id, origin);
						}
					}
				} catch(Exception e) {
					Origins.LOGGER.error("There was a problem reading Origin file " + id.toString() + " (skipping): " + e.getMessage());
				}
			});
			if(OriginRegistry.contains(id)) {
				Origin origin = OriginRegistry.get(id);
				hasConfigChanged.set(hasConfigChanged.get() | Origins.config.addToConfig(origin));
				if(Origins.config.isOriginDisabled(id)) {
					OriginRegistry.remove(id);
				} else {
					LinkedList<PowerType<?>> allPowers = new LinkedList<>();
					origin.getPowerTypes().forEach(allPowers::add);
					for(PowerType<?> powerType : allPowers) {
						if(Origins.config.isPowerDisabled(id, powerType.getIdentifier())) {
							origin.removePowerType(powerType);
						}
					}
				}
			}
		});
		Origins.LOGGER.info("Finished loading origins from data files. Registry contains " + OriginRegistry.size() + " origins.");
		if(hasConfigChanged.get()) {
			Origins.serializeConfig();
		}
	}

	// 以下两个方法随 IdentifiableResourceReloadListener 接口一并移除（改走 v1 PreparableReloadListener）：
	//   - getFabricId()          : v1 由 registerReloadListener(id, listener) 的 id 参数指定，不再由监听器自报
	//   - getFabricDependencies(): ⚠ 这就是那条**被静默忽略**的排序声明。它返回 {apoli:powers}，
	//     但 Fabric 只有已废弃的 ResourceManagerHelperImpl 会读它；Apoli 在 26.1 改用 v1 的
	//     addListenerOrdering 后，这里彻底失效 —— 正是本次 "unregistered power" 的成因。
	//     现在顺序统一由 Origins.registerResourceListeners 里的 addListenerOrdering 声明。
}
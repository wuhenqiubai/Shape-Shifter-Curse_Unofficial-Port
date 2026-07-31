package net.onixary.shapeShifterCurseFabric.integration.origins.component;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyPlayerSpawnPower;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.Origin;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.OriginLayer;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.OriginLayers;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.OriginRegistry;
import net.onixary.shapeShifterCurseFabric.integration.origins.power.OriginsCallbackPower;
import net.onixary.shapeShifterCurseFabric.integration.origins.registry.ModComponents;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.*;
import java.util.stream.Collectors;

public interface OriginComponent extends AutoSyncedComponent {

	boolean hasOrigin(OriginLayer layer);
	boolean hasAllOrigins();

	HashMap<OriginLayer, Origin> getOrigins();
	Origin getOrigin(OriginLayer layer);

	Optional<Boolean> hadOriginBefore();

	void setOrigin(OriginLayer layer, Origin origin);

	void sync();

	static void sync(Player player) {
		ModComponents.ORIGIN.sync(player);
		PowerHolderComponent.KEY.sync(player);
	}

	static void onChosen(Player player, boolean hadOriginBefore) {
		if(!hadOriginBefore) {
			PowerHolderComponent.getPowers(player, ModifyPlayerSpawnPower.class).forEach(ModifyPlayerSpawnPower::teleportToModifiedSpawn);
		}
		PowerHolderComponent.getPowers(player, OriginsCallbackPower.class).forEach(p -> p.onChosen(hadOriginBefore));
	}

	default boolean checkAutoChoosingLayers(Player player, boolean includeDefaults) {
		boolean choseOneAutomatically = false;
		ArrayList<OriginLayer> layers = new ArrayList<>();
		for(OriginLayer layer : OriginLayers.getLayers()) {
			if(layer.isEnabled()) {
				layers.add(layer);
			}
		}
		Collections.sort(layers);
		for(OriginLayer layer : layers) {
			boolean shouldContinue = false;
			if (layer.isEnabled() && !hasOrigin(layer)) {
				if (includeDefaults && layer.hasDefaultOrigin()) {
					setOrigin(layer, OriginRegistry.get(layer.getDefaultOrigin()));
					choseOneAutomatically = true;
					shouldContinue = true;
				} else if (layer.getOriginOptionCount(player) == 1 && layer.shouldAutoChoose()) {
					List<Origin> origins = layer.getOrigins(player).stream().map(OriginRegistry::get).filter(Origin::isChoosable).collect(Collectors.toList());
					if (origins.size() == 0) {
						List<Identifier> randomOrigins = layer.getRandomOrigins(player);
						setOrigin(layer, OriginRegistry.get(randomOrigins.get(player.getRandom().nextInt(randomOrigins.size()))));
					} else {
						setOrigin(layer, origins.get(0));
					}
					choseOneAutomatically = true;
					shouldContinue = true;
				} else if(layer.getOriginOptionCount(player) == 0) {
					shouldContinue = true;
				}
			} else {
				shouldContinue = true;
			}
			if(!shouldContinue) {
				break;
			}
		}
		return choseOneAutomatically;
	}
}
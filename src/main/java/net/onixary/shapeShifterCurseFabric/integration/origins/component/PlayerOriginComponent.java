package net.onixary.shapeShifterCurseFabric.integration.origins.component;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.Origin;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.OriginLayer;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.OriginLayers;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.OriginRegistry;
import net.onixary.shapeShifterCurseFabric.integration.origins.util.ChoseOriginCriterion;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PlayerOriginComponent implements OriginComponent {

    private Player player;
    private HashMap<OriginLayer, Origin> origins = new HashMap<>();

    private boolean hadOriginBefore = false;

    public PlayerOriginComponent(Player player) {
        this.player = player;
    }

    @Override
    public boolean hasAllOrigins() {
	    return OriginLayers.getLayers().stream().allMatch(layer -> !layer.isEnabled() || layer.getOrigins(player).isEmpty() || (origins.containsKey(layer) && origins.get(layer) != null && origins.get(layer) != Origin.EMPTY));
    }

    @Override
    public HashMap<OriginLayer, Origin> getOrigins() {
        return origins;
    }

    @Override
    public boolean hasOrigin(OriginLayer layer) {
        return origins != null && origins.containsKey(layer) && origins.get(layer) != null && origins.get(layer) != Origin.EMPTY;
    }

    @Override
    public Origin getOrigin(OriginLayer layer) {
        if(!origins.containsKey(layer)) {
            return null;
        }
        return origins.get(layer);
    }

    @Override
    public Optional<Boolean> hadOriginBefore() {
        return Optional.of(hadOriginBefore);
    }

    @Override
    public void setOrigin(OriginLayer layer, Origin origin) {
        Origin oldOrigin = getOrigin(layer);
        if(oldOrigin == origin) {
            return;
        }
        this.origins.put(layer, origin);
        PowerHolderComponent powerComponent = PowerHolderComponent.KEY.get(player);
        grantPowersFromOrigin(origin, powerComponent);
        if(oldOrigin != null) {
            powerComponent.removeAllPowersFromSource(oldOrigin.getIdentifier());
        }
        if(this.hasAllOrigins()) {
            this.hadOriginBefore = true;
        }
        if(player instanceof ServerPlayer spe) {
            ChoseOriginCriterion.INSTANCE.trigger(spe, origin);
        }
    }

    private void grantPowersFromOrigin(Origin origin, PowerHolderComponent powerComponent) {
        Identifier source = origin.getIdentifier();
        for(PowerType<?> powerType : origin.getPowerTypes()) {
            if(!powerComponent.hasPower(powerType, source)) {
                try {
                    powerComponent.addPower(powerType, source);
                } catch (Exception e) {
                    net.onixary.shapeShifterCurseFabric.integration.origins.Origins.LOGGER
                        .error("Failed to grant power {} from origin {}: {}", powerType.getIdentifier(), source, e.getMessage());
                }
            }
        }
    }

    private void revokeRemovedPowers(Origin origin, PowerHolderComponent powerComponent) {
        Identifier source = origin.getIdentifier();
        List<PowerType<?>> powersByOrigin = powerComponent.getPowersFromSource(source);
        powersByOrigin.stream().filter(p -> !origin.hasPowerType(p)).forEach(p -> powerComponent.removePower(p, source));
    }

    @Override
    public void readData(ValueInput input) {
        input.read("data", CompoundTag.CODEC).ifPresent(this::readFromNbt);
    }

    @Override
    public void writeData(ValueOutput output) {
        CompoundTag tag = new CompoundTag();
        this.writeToNbt(tag);
        output.store("data", CompoundTag.CODEC, tag);
    }

    public void readFromNbt(@NotNull CompoundTag compoundTag) {
        if(player == null) {
            Origins.LOGGER.error("Player was null in `fromTag`! This is a bug!");
	        return;
        }

        this.origins.clear();

        if(compoundTag.contains("Origin")) {
            try {
                OriginLayer defaultOriginLayer = OriginLayers.getLayer(Identifier.fromNamespaceAndPath(Origins.MODID, "origin"));
	            if (defaultOriginLayer == null) {
		            Origins.LOGGER.warn("Default origin layer not found");
		            return;
	            }

	            String originString = compoundTag.getStringOr("Origin", "");
	            Identifier originId = Identifier.tryParse(originString);
	            if (originId == null) {
		            Origins.LOGGER.warn("Invalid origin ID: {}", originString);
		            return;
	            }

	            Origin origin = OriginRegistry.get(originId);
	            if (origin != null) {
		            this.origins.put(defaultOriginLayer, origin);
	            } else {
		            Origins.LOGGER.warn("Origin not found: {}", originId);
	            }
            } catch(IllegalArgumentException e) {
	            String playerName = player.getDisplayName() != null ? player.getDisplayName().getString() : player.getName().getString();
	            Origins.LOGGER.warn("Player {} had old origin which could not be migrated: {}", playerName, compoundTag.getStringOr("Origin", ""));
            }
        } else {
	        Tag originLayerElement = compoundTag.get("OriginLayers");
	        if (!(originLayerElement instanceof ListTag originLayerList)) {
		        return;
	        }

	        for (int i = 0; i < originLayerList.size(); i++) {
		        CompoundTag layerTag = originLayerList.getCompoundOrEmpty(i);
		        String layerString = layerTag.getStringOr("Layer", "");
		        Identifier layerId = Identifier.tryParse(layerString);

		        if (layerId == null) {
			        String playerName = player.getDisplayName() != null ? player.getDisplayName().getString() : player.getName().getString();
			        Origins.LOGGER.warn("Invalid layer ID: {}, skipping for player {}", layerString, playerName);
			        continue;
		        }

		        OriginLayer layer = null;
		        try {
			        layer = OriginLayers.getLayer(layerId);
		        } catch (IllegalArgumentException e) {
			        String playerName = player.getDisplayName() != null ? player.getDisplayName().getString() : player.getName().getString();
			        Origins.LOGGER.warn("Could not find origin layer with id {}, which existed on the data of player {}.", layerId.toString(), playerName);
		        }

		        if (layer != null) {
			        String originString = layerTag.getStringOr("Origin", "");
			        Identifier originId = Identifier.tryParse(originString);

			        if (originId == null) {
				        String playerName = player.getDisplayName() != null ? player.getDisplayName().getString() : player.getName().getString();
				        Origins.LOGGER.warn("Invalid origin ID: {}, skipping for player {}", originString, playerName);
				        continue;
			        }

			        Origin origin = null;
			        try {
				        origin = OriginRegistry.get(originId);
			        } catch (IllegalArgumentException e) {
				        String playerName = player.getDisplayName() != null ? player.getDisplayName().getString() : player.getName().getString();
				        Origins.LOGGER.warn("Could not find origin with id {}, which existed on the data of player {}.", originId.toString(), playerName);
				        PowerHolderComponent powerComponent = PowerHolderComponent.KEY.get(player);
				        if (powerComponent != null) {
                            powerComponent.removeAllPowersFromSource(originId);
                        }
			        }

			        if (origin != null) {
				        if (!layer.contains(origin) && !origin.isSpecial()) {
					        String playerName = player.getDisplayName() != null ? player.getDisplayName().getString() : player.getName().getString();
					        Origins.LOGGER.warn("Origin with id {} is not in layer {} and is not special, but was found on {}, setting to EMPTY.", origin.getIdentifier().toString(), layer.getIdentifier().toString(), playerName);
					        origin = Origin.EMPTY;
					        PowerHolderComponent powerComponent = PowerHolderComponent.KEY.get(player);
					        if (powerComponent != null) {
                                powerComponent.removeAllPowersFromSource(originId);
                            }
				        }
				        this.origins.put(layer, origin);
			        }
		        }
	        }
        }
        this.hadOriginBefore = compoundTag.getBooleanOr("HadOriginBefore", false);

        if(!player.level().isClientSide()) {
            PowerHolderComponent powerComponent = PowerHolderComponent.KEY.get(player);
	        if (powerComponent == null) {
		        Origins.LOGGER.warn("PowerHolderComponent is null for player {}", player.getName().getString());
		        return;
	        }

            for(Origin origin : origins.values()) {
                // Grants powers only if the player doesn't have them yet from the specific Origin source.
                // Needed in case the origin was set before the update to Apoli happened.
                grantPowersFromOrigin(origin, powerComponent);
            }
            for(Origin origin : origins.values()) {
                revokeRemovedPowers(origin, powerComponent);
            }

            // Compatibility with old worlds:
            // Loads power data from Origins tag, whereas new versions
            // store the data in the Apoli tag.
            if(compoundTag.contains("Powers")) {
	            Tag powersElement = compoundTag.get("Powers");
	            if (!(powersElement instanceof ListTag powerList)) {
		            return;
	            }

                for(int i = 0; i < powerList.size(); i++) {
                    CompoundTag powerTag = powerList.getCompoundOrEmpty(i);
	                String powerTypeString = powerTag.getStringOr("Type", "");
	                Identifier powerTypeId = Identifier.tryParse(powerTypeString);

	                if (powerTypeId == null) {
		                Origins.LOGGER.warn("Invalid power type ID: {}", powerTypeString);
		                continue;
	                }

                    try {
                        PowerType<?> type = PowerTypeRegistry.get(powerTypeId);
                        if(powerComponent.hasPower(type)) {
                            Tag data = powerTag.get("Data");
                            try {
                                powerComponent.getPower(type).fromTag(data);
                            } catch(ClassCastException e) {
                                // Occurs when power was overriden by data pack since last world load
                                // to be a power type which uses different data class.
	                            String playerName = player.getDisplayName() != null ? player.getDisplayName().getString() : player.getName().getString();
	                            Origins.LOGGER.warn("Data type of \"{}\" changed, skipping data for that power on player {}", powerTypeId, playerName);
                            }
                        }
                    } catch(IllegalArgumentException e) {
	                    Origins.LOGGER.warn("Power data of unregistered power \"{}\" found on player, skipping...", powerTypeId);
                    }
                }
            }
        }
    }

    public void writeToNbt(@NotNull CompoundTag compoundTag) {
        ListTag originLayerList = new ListTag();
        for(Map.Entry<OriginLayer, Origin> entry : origins.entrySet()) {
            CompoundTag layerTag = new CompoundTag();
            layerTag.putString("Layer", entry.getKey().getIdentifier().toString());
            layerTag.putString("Origin", entry.getValue().getIdentifier().toString());
            originLayerList.add(layerTag);
        }
        compoundTag.put("OriginLayers", originLayerList);
        compoundTag.putBoolean("HadOriginBefore", this.hadOriginBefore);
    }

    @Override
    public void sync() {
        OriginComponent.sync(this.player);
    }
}
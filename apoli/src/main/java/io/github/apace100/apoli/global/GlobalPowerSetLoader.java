package io.github.apace100.apoli.global;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.calio.data.SerializableData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.*;
import java.util.stream.Collectors;

public class GlobalPowerSetLoader extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloadListener {

    public static final Set<ResourceLocation> DEPENDENCIES = Set.of(Apoli.identifier("powers"));

    public static List<GlobalPowerSet> ALL = new LinkedList<>();

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    private final HolderLookup.Provider provider;

    public GlobalPowerSetLoader(HolderLookup.Provider provider) {
        super(GSON, "global_powers");
        this.provider = provider;

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            GlobalPowerSetUtil.applyGlobalPowers(entity);
        });
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> prepared, ResourceManager manager, ProfilerFiller profiler) {
        ALL.clear();
        prepared.forEach((id, json) -> {
            if(json.isJsonObject()) {
                SerializableData.Instance data = GlobalPowerSet.DATA.read(json.getAsJsonObject(), this.provider);
                GlobalPowerSet gps = GlobalPowerSet.FACTORY.fromData(data);
                List<PowerType<?>> invalidPowerTypes = gps.validate();
                if(invalidPowerTypes.size() > 0) {
                    Apoli.LOGGER.error("Global power set \"{}\" contained invalid powers: {}",
                        id, invalidPowerTypes.stream()
                            .map(PowerType::getIdentifier)
                            .map(ResourceLocation::toString)
                            .collect(Collectors.joining(", ")));
                }
                ALL.add(gps);
            }
        });
        Apoli.LOGGER.info("Loaded " + ALL.size() + " global power sets.");
    }

    @Override
    public ResourceLocation getFabricId() {
        return Apoli.identifier("global_powers");
    }

    @Override
    public Collection<ResourceLocation> getFabricDependencies() {
        return DEPENDENCIES;
    }
}
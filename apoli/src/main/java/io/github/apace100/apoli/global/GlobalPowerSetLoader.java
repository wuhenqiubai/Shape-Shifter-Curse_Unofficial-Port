package io.github.apace100.apoli.global;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.PowerType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.*;
import java.util.stream.Collectors;

public class GlobalPowerSetLoader extends SimpleJsonResourceReloadListener<GlobalPowerSet> implements IdentifiableResourceReloadListener {
    public static final Set<Identifier> DEPENDENCIES = Set.of(Apoli.identifier("powers"));

    public static List<GlobalPowerSet> ALL = new LinkedList<>();

    public GlobalPowerSetLoader() {
        super(GlobalPowerSet.CODEC, FileToIdConverter.json("global_powers"));

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            GlobalPowerSetUtil.applyGlobalPowers(entity);
        });
    }

    @Override
    protected void apply(Map<Identifier, GlobalPowerSet> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        ALL.clear();
        object.forEach((id, gps) -> {
            List<PowerType<?>> invalidPowerTypes = gps.validate();
            if(invalidPowerTypes.size() > 0) {
                Apoli.LOGGER.error("Global power set \"{}\" contained invalid powers: {}",
                    id, invalidPowerTypes.stream()
                        .map(PowerType::getIdentifier)
                        .map(Identifier::toString)
                        .collect(Collectors.joining(", ")));
            }

            ALL.add(gps);
        });
        Apoli.LOGGER.info("Loaded " + ALL.size() + " global power sets.");
    }

    @Override
    public Identifier getFabricId() {
        return Apoli.identifier("global_powers");
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return DEPENDENCIES;
    }
}

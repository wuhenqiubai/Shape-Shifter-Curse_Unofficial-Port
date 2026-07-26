package net.onixary.shapeShifterCurseFabric.player_form.ability;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class RegFormLayer {
    public static HashMap<ResourceLocation, IFormLayerGroup> layerGroupRegistry = new HashMap<>();
    public static HashMap<ResourceLocation, IFormLayer> layerRegistry = new HashMap<>();

    public static @Nullable IFormLayer getLayer(ResourceLocation id) {
        return layerRegistry.get(id);
    }

    public static @Nullable IFormLayerGroup getLayerGroup(ResourceLocation id) {
        return layerGroupRegistry.get(id);
    }

    public static @NotNull IFormLayer getLayerOrDefault(ResourceLocation id, @NotNull IFormLayer defaultLayer) {
        return layerRegistry.getOrDefault(id, defaultLayer);
    }

    public static @NotNull IFormLayerGroup getLayerGroupOrDefault(ResourceLocation id, @NotNull IFormLayerGroup defaultLayerGroup) {
        return layerGroupRegistry.getOrDefault(id, defaultLayerGroup);
    }
}
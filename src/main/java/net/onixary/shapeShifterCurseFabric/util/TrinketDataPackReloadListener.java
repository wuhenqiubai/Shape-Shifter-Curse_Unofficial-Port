package net.onixary.shapeShifterCurseFabric.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import java.io.IOException;

public class TrinketDataPackReloadListener implements SimpleSynchronousResourceReloadListener {
    @Override
    public ResourceLocation getFabricId() {
        return ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "accessory_power");
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        TrinketUtils.clearAccessoryPower();
        manager.listResources("accessory_power", identifier -> identifier.getPath().endsWith(".json")).forEach((identifier, resource) -> {
            JsonObject accessoryData;
            try {
                accessoryData = JsonParser.parseString(new String(resource.open().readAllBytes())).getAsJsonObject();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            TrinketUtils.loadAccessoryPowerData(accessoryData);
        });
    }
}

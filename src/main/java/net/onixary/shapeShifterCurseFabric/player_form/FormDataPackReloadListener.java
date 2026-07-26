package net.onixary.shapeShifterCurseFabric.player_form;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class FormDataPackReloadListener implements SimpleSynchronousResourceReloadListener {
    @Override
    public ResourceLocation getFabricId() {
        return ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "ssc_form");
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        // ssc_form
        RegPlayerForms.ClearAllDynamicPlayerForms();
        manager.listResources("ssc_form", identifier -> identifier.getPath().endsWith(".json")).forEach((identifier, resource) -> {
            // shape-shifter-curse:ssc_form/example.json -> shape-shifter-curse:example
            ResourceLocation formID = ResourceLocation.fromNamespaceAndPath(identifier.getNamespace(), identifier.getPath().replace(".json", "").substring(9));
            JsonObject formData;
            try {
                formData = JsonParser.parseString(new String(resource.open().readAllBytes())).getAsJsonObject();
            } catch (IOException e) {
                ShapeShifterCurseFabric.LOGGER.error("Failed to load form data for " + formID);
                return;
            }
            try {
                RegPlayerForms.registerDynamicPlayerForm(DynamicForm.fromJson(formID, formData));
            } catch (Exception e) {
                ShapeShifterCurseFabric.LOGGER.error("Failed to load form data for " + formID);
            }
            ShapeShifterCurseFabric.LOGGER.info("Loaded form data for " + formID);
        });
        // origins_power_extra
        FormUtils.extraPowerRegistry.clear();
        manager.listResources("origins_power_extra", identifier -> identifier.getPath().endsWith(".json")).forEach((identifier, resource) -> {
            ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(identifier.getNamespace(), identifier.getPath().replace(".json", "").substring(20));
            JsonObject ExtraPowerData;
            ResourceLocation LayerID;
            ResourceLocation OriginID;
            List<ResourceLocation> ExtraPowerIDs = new LinkedList<>();
            try {
                ExtraPowerData = JsonParser.parseString(new String(resource.open().readAllBytes())).getAsJsonObject();
                LayerID = ResourceLocation.tryParse(ExtraPowerData.get("TargetLayerID").getAsString());
                OriginID = ResourceLocation.tryParse(ExtraPowerData.get("TargetOriginsID").getAsString());
                JsonArray PowerIDs = ExtraPowerData.get("ExtraPowers").getAsJsonArray();
                for (int i = 0; i < PowerIDs.size(); i++) {
                    ExtraPowerIDs.add(ResourceLocation.tryParse(PowerIDs.get(i).getAsString()));
                }
            } catch (Exception e) {
                ShapeShifterCurseFabric.LOGGER.error("Failed to load extra power data for " + ID);
                return;
            }
            if (!ExtraPowerIDs.isEmpty() && LayerID != null && OriginID != null) {
                FormUtils.registerExtraPower(ID, new FormUtils.ExtraPower(LayerID, OriginID, ExtraPowerIDs));
            }
            ShapeShifterCurseFabric.LOGGER.info("Loaded extra power data for " + ID);
        });
    }
}

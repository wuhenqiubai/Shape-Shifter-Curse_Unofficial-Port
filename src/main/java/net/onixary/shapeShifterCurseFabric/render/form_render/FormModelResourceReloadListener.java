package net.onixary.shapeShifterCurseFabric.render.form_render;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormModelResourceReloadListener implements SimpleSynchronousResourceReloadListener {
    @Override
    public ResourceLocation getFabricId() {
        return ShapeShifterCurseFabric.identifier("ssc_form_model");
    }

	public static final ResourceLocation defaultLayer = ResourceLocation.fromNamespaceAndPath("origins", "origin");

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        FormRenderUtils.formRendererRegistry.clear();
        HashMap<ResourceLocation, HashMap<ResourceLocation, List<JsonObject>>> jsonMap = new HashMap<>();
        // 读取文件
        Map<ResourceLocation, Resource> resourceMap = manager.listResources("ssc_form_model", identifier -> identifier.getPath().endsWith(".json"));
        for (ResourceLocation identifier : resourceMap.keySet()) {
            Resource resource = resourceMap.get(identifier);
            // shape-shifter-curse:ssc_form_model/namespace.layer.namespace.form.json -> namespace.layer.namespace.form
            String realPath = identifier.getPath().substring(identifier.getPath().indexOf('/')+1, identifier.getPath().lastIndexOf('.'));
            // namespace.layer.namespace.form -> Identifier(namespace, layer), Identifier(namespace, form)
            String[] parts = realPath.split("\\.");
            ResourceLocation LayerID = defaultLayer;
            ResourceLocation FormID = null;
            if (parts.length == 2) {
                FormID = ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
            } else if (parts.length == 4) {
                LayerID = ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
                FormID = ResourceLocation.fromNamespaceAndPath(parts[2], parts[3]);
            } else {
                ShapeShifterCurseFabric.LOGGER.warn("Invalid ssc_form_model json file: " + identifier);
            }
            try {
                JsonObject json = JsonParser.parseString(new String(resourceMap.get(identifier).open().readAllBytes())).getAsJsonObject();
                jsonMap.computeIfAbsent(LayerID, k -> new HashMap<>()).computeIfAbsent(FormID, k -> new ArrayList<>()).add(json);
            } catch (IOException e) {
                ShapeShifterCurseFabric.LOGGER.error("Error reading ssc_form_model json file: " + identifier, e);
            };
        }
        // 解析文件
        // 最高load_priority的json会被加载 同load_priority先到者会被加载 无论能否被正常加载(好进行调试)
        for (ResourceLocation layerID : jsonMap.keySet()) {
            for (ResourceLocation formID : jsonMap.get(layerID).keySet()) {
                List<JsonObject> jsonList = jsonMap.get(layerID).get(formID);
                int MaxLoadPriority = Integer.MIN_VALUE;
                JsonObject HighestLoadPriorityJson = null;
                for (JsonObject json : jsonList) {
                    int loadPriority = GsonHelper.getAsInt(json, "load_priority", 0);
                    if (loadPriority > MaxLoadPriority) {
                        MaxLoadPriority = loadPriority;
                        HighestLoadPriorityJson = json;
                    }
                }
                if (HighestLoadPriorityJson == null) {
                    // 一般不可能发生 除非有人修改了这个函数 或者故意写一个-2147483648的加载顺序的Json
                    ShapeShifterCurseFabric.LOGGER.warn("No ssc_form_model json file found for layer: " + layerID + ", form: " + formID);
                    continue;
                }
                try {
                    FormRenderUtils.registerFormRenderer(layerID, formID, new FormRenderer(HighestLoadPriorityJson));
                } catch (Exception e) {
                    ShapeShifterCurseFabric.LOGGER.error("Error loading ssc_form_model json file: " + layerID + ", form: " + formID, e);
                }
            }
        }
    }
}

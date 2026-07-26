package net.onixary.shapeShifterCurseFabric.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MorphScaleTagLoader implements SimpleSynchronousResourceReloadListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShapeShifterCurseFabric.MOD_ID + ":MorphScaleTagLoader");
    private static volatile Set<String> morphScaleItems = Collections.emptySet();

    public static Set<String> getMorphScaleItems() {
        return morphScaleItems;
    }

    @Override
    public ResourceLocation getFabricId() {
        return ShapeShifterCurseFabric.identifier("morph_scale_tag_loader");
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        var resources = manager.listResources("tags/items",
                id -> id.getNamespace().equals(ShapeShifterCurseFabric.MOD_ID)
                        && id.getPath().endsWith("morph_scale_item.json"));
        if (resources.isEmpty()) {
            LOGGER.warn("morph_scale_item tag not found, using empty set");
            morphScaleItems = Collections.emptySet();
            return;
        }

        Set<String> items = new HashSet<>();
        for (var entry : resources.entrySet()) {
            try (var reader = new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                for (JsonElement value : json.getAsJsonArray("values")) {
                    String id;
                    if (value.isJsonObject()) {
                        id = value.getAsJsonObject().get("id").getAsString();
                    } else {
                        id = value.getAsString();
                    }
                    if (!id.startsWith("#")) {
                        items.add(id);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to read morph_scale_item tag from {}", entry.getKey(), e);
            }
        }
        morphScaleItems = Collections.unmodifiableSet(items);
        LOGGER.info("Loaded {} morph scale items from tag", items.size());
    }
}

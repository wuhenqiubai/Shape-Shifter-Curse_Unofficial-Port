package net.onixary.shapeShifterCurseFabric.integration.origins.registry;

import io.github.apace100.calio.Calio;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Ensures origins namespace tags are available in Calio's REGISTRY_TAGS
 * before power data is loaded, preventing parse-time tag validation failures.
 */
public class OriginsTagLoader implements SimpleSynchronousResourceReloadListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(OriginsTagLoader.class);

    @Override
    public @NonNull Identifier getFabricId() {
        return Origins.identifier("origins_tags");
    }

    @Override
    public void onResourceManagerReload(@NonNull ResourceManager manager) {
        Map<TagKey<?>, Collection<Holder<?>>> registryTags = Calio.REGISTRY_TAGS.get();
        if (registryTags == null) {
            registryTags = new HashMap<>();
            Calio.REGISTRY_TAGS.set(registryTags);
        }

        int count = 0;

        // Scan ALL namespaces for tag files to ensure they're available at parse time
        count += registerTags(registryTags, Registries.ITEM, manager, "items");
        count += registerTags(registryTags, Registries.BLOCK, manager, "blocks");
        count += registerTags(registryTags, Registries.ENTITY_TYPE, manager, "entity_type");
        count += registerTags(registryTags, Registries.DAMAGE_TYPE, manager, "damage_type");

        LOGGER.info("Registered {} missing tags across all namespaces", count);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private int registerTags(Map<TagKey<?>, Collection<Holder<?>>> registryTags,
                             ResourceKey<? extends Registry<?>> registryKey,
                             ResourceManager manager,
                             String registryDir) {
        int count = 0;
        for (Identifier path : manager.listResources("tags/" + registryDir,
                id -> id.getPath().endsWith(".json"))
                .keySet()) {
            // path: origin/tags/items/ignore_diet.json
            String ns = path.getNamespace();
            String fileName = path.getPath().substring(path.getPath().lastIndexOf('/') + 1).replace(".json", "");
            TagKey<?> tagKey = TagKey.create((ResourceKey) registryKey, Identifier.fromNamespaceAndPath(ns, fileName));
            if (!registryTags.containsKey(tagKey)) {
                registryTags.put(tagKey, Collections.emptyList());
                count++;
            }
        }
        return count;
    }
}
package net.onixary.shapeShifterCurseFabric.integration.origins.registry;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
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
        // Calio 1.11.2 移除了 REGISTRY_TAGS 缓存机制，改用原版 tag 系统。
        // 该 listener 仅保留 resource listener 注册顺序（在 power 加载前），无需再手动预注册 tag。
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
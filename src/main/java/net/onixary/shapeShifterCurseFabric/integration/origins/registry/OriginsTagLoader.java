package net.onixary.shapeShifterCurseFabric.integration.origins.registry;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class OriginsTagLoader implements SimpleSynchronousResourceReloadListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(OriginsTagLoader.class);

    @Override
    public ResourceLocation getFabricId() {
        return Origins.identifier("origins_tags");
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private int registerTags(Map<TagKey<?>, Collection<Holder<?>>> registryTags,
                             ResourceKey<? extends Registry<?>> registryKey,
                             ResourceManager manager,
                             String registryDir) {
        int count = 0;
        for (ResourceLocation path : manager.listResources("tags/" + registryDir,
                id -> id.getPath().endsWith(".json"))
                .keySet()) {
            String ns = path.getNamespace();
            String fileName = path.getPath().substring(path.getPath().lastIndexOf('/') + 1).replace(".json", "");
            TagKey<?> tagKey = TagKey.create((ResourceKey) registryKey, ResourceLocation.fromNamespaceAndPath(ns, fileName));
            if (!registryTags.containsKey(tagKey)) {
                registryTags.put(tagKey, Collections.emptyList());
                count++;
            }
        }
        return count;
    }
}
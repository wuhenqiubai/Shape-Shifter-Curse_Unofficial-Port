package net.onixary.shapeShifterCurseFabric.integration.origins.registry;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OriginsTagLoader implements SimpleSynchronousResourceReloadListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(OriginsTagLoader.class);

    @Override
    public ResourceLocation getFabricId() {
        return Origins.identifier("origins_tags");
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
    }

}
package io.github.apace100.calio.data;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;

/***
 * Like JsonDataLoader, but provides a list of elements with an identifier, each element being loaded by a different
 * data pack. This allows overriding and merging several data files into one, similar to how tags work. There is no
 * guarantee on the order of the resulting list, so make sure to include some kind of "priority" system.
 */
public abstract class MultiJsonDataLoader extends SimplePreparableReloadListener<Map<ResourceLocation, List<JsonElement>>> {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int FILE_SUFFIX_LENGTH = ".json".length();
    private final Gson gson;
    private final String dataType;

    public MultiJsonDataLoader(Gson gson, String dataType) {
        this.gson = gson;
        this.dataType = dataType;
    }

    protected Map<ResourceLocation, List<JsonElement>> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, List<JsonElement>> map = Maps.newHashMap();
        int i = this.dataType.length() + 1;
        Iterator<Map.Entry<ResourceLocation, Resource>> var5 = resourceManager.listResources(this.dataType, (id) -> {
            return id.getPath().endsWith(".json");
        }).entrySet().iterator();
        Set<String> resourcesHandled = new HashSet<>();
        while(var5.hasNext()) {
            ResourceLocation identifier = var5.next().getKey();
            String string = identifier.getPath();
            ResourceLocation identifier2 = ResourceLocation.fromNamespaceAndPath(identifier.getNamespace(), string.substring(i, string.length() - FILE_SUFFIX_LENGTH));
            resourcesHandled.clear();
            resourceManager.getResourceStack(identifier).forEach(resource -> {
                if(!resourcesHandled.contains(resource.sourcePackId())) {
                    resourcesHandled.add(resource.sourcePackId());
                    try {
                        Throwable var10 = null;
                        try {
                            InputStream inputStream = resource.open();
                            Throwable var12 = null;

                            try {
                                Reader reader = resource.openAsReader();
                                Throwable var14 = null;

                                try {
                                    JsonElement jsonElement = (JsonElement) GsonHelper.fromJson(this.gson, (Reader)reader, (Class)JsonElement.class);
                                    if (jsonElement != null) {
                                        if(map.containsKey(identifier2)) {
                                            map.get(identifier2).add(jsonElement);
                                        } else {
                                            List<JsonElement> elementList = new LinkedList<>();
                                            elementList.add(jsonElement);
                                            map.put(identifier2, elementList);
                                        }
                                    } else {
                                        LOGGER.error("Couldn't load data file {} from {} as it's null or empty", identifier2, identifier);
                                    }
                                } catch (Throwable var62) {
                                    var14 = var62;
                                    throw var62;
                                } finally {
                                    if (reader != null) {
                                        if (var14 != null) {
                                            try {
                                                reader.close();
                                            } catch (Throwable var61) {
                                                var14.addSuppressed(var61);
                                            }
                                        } else {
                                            reader.close();
                                        }
                                    }

                                }
                            } catch (Throwable var64) {
                                var12 = var64;
                                throw var64;
                            } finally {
                                if (inputStream != null) {
                                    if (var12 != null) {
                                        try {
                                            inputStream.close();
                                        } catch (Throwable var60) {
                                            var12.addSuppressed(var60);
                                        }
                                    } else {
                                        inputStream.close();
                                    }
                                }

                            }
                        } catch (Throwable var66) {
                            var10 = var66;
                            throw var66;
                        }
                    } catch (IllegalArgumentException | IOException | JsonParseException var68) {
                        LOGGER.error("Couldn't parse data file {} from {}", identifier2, identifier, var68);
                    }
                }
            });
        }

        return map;
    }
}

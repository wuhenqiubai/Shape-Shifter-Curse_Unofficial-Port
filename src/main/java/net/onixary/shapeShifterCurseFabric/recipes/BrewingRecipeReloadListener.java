package net.onixary.shapeShifterCurseFabric.recipes;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import java.io.IOException;

public class BrewingRecipeReloadListener implements SimpleSynchronousResourceReloadListener {
    @Override
    public Identifier getFabricId() {
        return Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "dynamic_brewing_recipes");
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        BrewingRecipeUtils.onLoadDynamicBrewingRecipesStart();
        manager.listResources("dynamic_brewing_recipes", identifier -> identifier.getPath().endsWith(".json")).forEach((identifier, resource) -> {
            JsonObject accessoryData;
            try {
                accessoryData = JsonParser.parseString(new String(resource.open().readAllBytes())).getAsJsonObject();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            BrewingRecipeUtils.registerPotionRecipe(accessoryData);
        });
        BrewingRecipeUtils.onLoadDynamicBrewingRecipesEnd();
    }
}
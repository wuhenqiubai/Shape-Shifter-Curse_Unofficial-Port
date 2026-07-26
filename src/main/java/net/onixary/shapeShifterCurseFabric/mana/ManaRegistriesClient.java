package net.onixary.shapeShifterCurseFabric.mana;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

@Environment(EnvType.CLIENT)
public class ManaRegistriesClient {

    private static final HashMap<ResourceLocation, IManaRender> manaRenderRegistry = new HashMap<>();

    public static void registerManaTypeRender(ResourceLocation identifier, @NotNull IManaRender render) {
        manaRenderRegistry.put(identifier, render);
    }

    public static boolean hasManaRender(@Nullable ResourceLocation identifier) {
        return manaRenderRegistry.containsKey(identifier);
    }

    public static @Nullable IManaRender getManaRender(@Nullable ResourceLocation identifier) {
        return manaRenderRegistry.get(identifier);
    }

    static {
        registerManaTypeRender(ManaRegistries.FAMILIAR_FOX_MANA, new FamiliarFoxManaBar());
        registerManaTypeRender(ManaRegistries.WEB_RESOURCE, new WebResourceBar());
        registerManaTypeRender(ManaRegistries.DP_MANA, new FamiliarFoxManaBar());
    }

    public static void register() {}
}

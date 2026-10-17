package net.onixary.shapeShifterCurseFabric.cursed_moon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

public final class CursedMoonSkyTextures {
    private static AbstractTexture cursedMoonTexture;

    private CursedMoonSkyTextures() {
    }

    public static void preload() {
        if (cursedMoonTexture == null) {
            cursedMoonTexture = Minecraft.getInstance().getTextureManager()
                    .getTexture(Identifier.fromNamespaceAndPath(MOD_ID, "textures/environment/cursed_moon_phases.png"));
        }
    }

    public static AbstractTexture getCursedMoonTexture() {
        return cursedMoonTexture;
    }
}

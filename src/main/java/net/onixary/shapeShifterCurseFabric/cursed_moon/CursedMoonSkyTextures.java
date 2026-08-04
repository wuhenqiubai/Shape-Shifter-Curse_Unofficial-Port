package net.onixary.shapeShifterCurseFabric.cursed_moon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

/**
 * 咒月月亮纹理预加载（普通类，非 Mixin —— Mixin 类不能被业务代码直接引用，
 * 否则 classload 抛 IllegalClassLoadError）。必须在渲染 pass 外上传纹理：
 * TextureManager.getTexture 会做 GL 上传，renderMoon 的 render pass 内调用会崩。
 */
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

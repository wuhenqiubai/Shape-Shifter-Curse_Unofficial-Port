package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.onixary.shapeShifterCurseFabric.cursed_moon.CursedMoon;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightTexture.class)
public abstract class CursedMoonLightmapMixin implements AutoCloseable{
    // 1.21.11: updateLightTexture(float) 单参，blockLightRedFlicker 字段读取点捕获最终光照颜色 vector3f
    @Inject(
        method = "updateLightTexture(F)V",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/LightTexture;blockLightRedFlicker:F")
    )
    private void update(float f, CallbackInfo ci, @Local Vector3f vector3f) {
        Minecraft client = Minecraft.getInstance();
        if (CursedMoon.isCursedMoonDay(client.level)) {
            Vector3f modifiedColor = new Vector3f(1.0F, 0.24F, 0.82F);
            float skyBlend = 1.0F - f - (client.level != null ? client.level.getRainLevel(1.0F) : 0.0F);
            vector3f.lerp(modifiedColor, skyBlend);
        }
    }
}

package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.onixary.shapeShifterCurseFabric.cursed_moon.CursedMoon;
import org.joml.Vector3f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightTexture.class)
public abstract class CursedMoonLightmapMixin implements AutoCloseable{
    // 1.21.11: updateLightTexture(float) 单参，blockLightRedFlicker 字段读取点捕获最终光照颜色 vector3f
    @Inject(
        method = "updateLightTexture(F)V",
        at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
                 target = "Lnet/minecraft/client/renderer/LightTexture;blockLightRedFlicker:F")
    )
    private void update(float partialTick, CallbackInfo ci, @Local Vector3f vector3f) {
        Minecraft client = Minecraft.getInstance();
        ClientLevel clientLevel = client.level;
        if (clientLevel == null || !CursedMoon.isCursedMoonDay(clientLevel)) {
            return;
        }
        // ⚠ 天空亮度必须取 SKY_LIGHT_FACTOR，不能用方法参数 partialTick！
        // 1.21.1 原实现取的是 clientLevel.getSkyDarken(1.0F)：白天≈1.0 → skyBlend≈0（不染色）、
        // 夜晚≈0.2 → skyBlend≈0.8（染成咒月色）。1.21.11 该值由 EnvironmentAttributes.SKY_LIGHT_FACTOR
        // 取代（LightTexture.updateLightTexture 内部就是取它算 SKY_LIGHT_FACTOR）。
        // partialTick 是 0~1 的帧插值系数（帧末常接近 1），拿它当天空亮度会让 skyBlend≈0 ——
        // 表现为咒月时全局光照完全不染色。
        Camera camera = client.gameRenderer.getMainCamera();
        float skyLightFactor = camera.attributeProbe().getValue(EnvironmentAttributes.SKY_LIGHT_FACTOR, partialTick);
        float skyBlend = 1.0F - skyLightFactor - clientLevel.getRainLevel(1.0F);
        vector3f.lerp(new Vector3f(1.0F, 0.24F, 0.82F), skyBlend);
    }
}

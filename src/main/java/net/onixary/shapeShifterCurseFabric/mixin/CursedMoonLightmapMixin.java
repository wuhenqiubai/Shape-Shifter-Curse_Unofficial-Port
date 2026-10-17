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
        Camera camera = client.gameRenderer.getMainCamera();
        float skyLightFactor = camera.attributeProbe().getValue(EnvironmentAttributes.SKY_LIGHT_FACTOR, partialTick);
        float skyBlend = 1.0F - skyLightFactor - clientLevel.getRainLevel(1.0F);
        vector3f.lerp(new Vector3f(1.0F, 0.24F, 0.82F), skyBlend);
    }
}

package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.NightVisionPower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Optional;

@Mixin(LightTexture.class)
@Environment(EnvType.CLIENT)
public abstract class LightmapTextureManagerMixin implements AutoCloseable {

    @Shadow @Final private Minecraft minecraft;

    @ModifyVariable(method = "updateLightTexture", at = @At(value = "STORE"), ordinal = 6)
    private float nightVisionPowerEffect(float value) {
        Optional<Float> nightVisionStrength = PowerHolderComponent.KEY.get(minecraft.player).getPowers(NightVisionPower.class).stream().filter(NightVisionPower::isActive).map(NightVisionPower::getStrength).max(Float::compareTo);
        return nightVisionStrength.map(aFloat -> Math.max(aFloat, value)).orElse(value);
    }
}

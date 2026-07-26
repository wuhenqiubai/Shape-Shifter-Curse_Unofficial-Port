package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.onixary.shapeShifterCurseFabric.cursed_moon.CursedMoon;
import net.onixary.shapeShifterCurseFabric.cursed_moon.CursedMoonClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

@Mixin(value = LevelRenderer.class, priority = 949)
public class MoonPhaseRenderMixin {
    @Unique
    private final ResourceLocation Vanilla_MOON_PHASES = ResourceLocation.parse("textures/environment/moon_phases.png");

    @Unique
    private final ResourceLocation CURSED_MOON_PHASES = ResourceLocation.fromNamespaceAndPath(MOD_ID,"textures/environment/cursed_moon_phases.png");

    @Unique
    public ResourceLocation getMoonIdentifier() {
        Minecraft client = Minecraft.getInstance();
        if (client.level != null) {
            return CursedMoon.isCursedMoonDay(client.level) ? CURSED_MOON_PHASES : Vanilla_MOON_PHASES;
        }
        return CursedMoonClient.isCursedMoon ? CURSED_MOON_PHASES : Vanilla_MOON_PHASES;
    }

    @ModifyArg(method = "renderSky(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V", ordinal = 1))
    private ResourceLocation getMoonPhaseTexture(ResourceLocation identifier) {
        ResourceLocation moonId = getMoonIdentifier();
        return moonId != null ? moonId : identifier;
    }
}
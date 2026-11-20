package io.github.apace100.apoli.mixin.legacy.hud_power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.OverrideHudTexturePower;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Optional;

@Mixin(Hud.class)
public abstract class HudMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    // TODO Origins-Legacy: Map GUI icons correctly
    @ModifyArg(method = "extractArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"), index = 1)
    private static Identifier changeStatusBarTextures(Identifier original) {
        Optional<OverrideHudTexturePower> power = PowerHolderComponent.getPowers(Minecraft.getInstance().player, OverrideHudTexturePower.class).stream().findFirst();
        if (power.isPresent()) {
            return power.get().getStatusBarTexture();
        }
        return original;
    }

    @ModifyArg(method = "extractHeart", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"), index = 1)
    public Identifier changeHearts(Identifier original)
    {
        Optional<OverrideHudTexturePower> power = PowerHolderComponent.getPowers(this.minecraft.player, OverrideHudTexturePower.class).stream().findFirst();
        if (power.isPresent()) {
            return power.get().getStatusBarTexture();
        }
        return original;
    }

    @ModifyArg(method = "extractCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 0), index = 1)
    public Identifier changeCrosshair(Identifier original) {
        Optional<OverrideHudTexturePower> power = PowerHolderComponent.getPowers(this.minecraft.player, OverrideHudTexturePower.class).stream().findFirst();
        if (power.isPresent()) {
            return power.get().getStatusBarTexture();
        }
        return original;
    }

    @ModifyArg(method = "extractVehicleHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"), index = 1)
    public Identifier changeMountHealth(Identifier original) {
        Optional<OverrideHudTexturePower> power = PowerHolderComponent.getPowers(this.minecraft.player, OverrideHudTexturePower.class).stream().findFirst();
        if (power.isPresent()) {
            return power.get().getStatusBarTexture();
        }
        return original;
    }
}

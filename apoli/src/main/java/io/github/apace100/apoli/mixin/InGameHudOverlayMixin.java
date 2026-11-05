package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.OverlayPower;
import io.github.apace100.apoli.screen.GameHudRender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 详见 docs/connector-neoforge-compat.md
@Mixin(Gui.class)
@Environment(EnvType.CLIENT)
public class InGameHudOverlayMixin {

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "extractCrosshair", at = @At("HEAD"))
    private void apoli$renderOverlay(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        boolean hudHidden = minecraft.options.hideGui;
        boolean thirdPerson = !minecraft.options.getCameraType().isFirstPerson();
        PowerHolderComponent.withPower(minecraft.getCameraEntity(), OverlayPower.class, p -> {
            if(p.getDrawPhase() != OverlayPower.DrawPhase.BELOW_HUD) {
                return false;
            }
            if(hudHidden && p.doesHideWithHud()) {
                return false;
            }
            if(thirdPerson && !p.shouldBeVisibleInThirdPerson()) {
                return false;
            }
            return true;
        }, p -> p.render(guiGraphics));

        for(GameHudRender hudRender : GameHudRender.HUD_RENDERS) {
            hudRender.render(guiGraphics, deltaTracker.getGameTimeDeltaPartialTick(true));
        }
    }
}

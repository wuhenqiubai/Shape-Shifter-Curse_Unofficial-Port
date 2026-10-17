package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.onixary.shapeShifterCurseFabric.client.ShapeShifterCurseFabricClient;
import net.onixary.shapeShifterCurseFabric.magic.MagicGUIRender;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void ssc$onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (window == Minecraft.getInstance().getWindow().handle()) {
            double d = ((Boolean) this.minecraft.options.discreteMouseScroll().get() ? Math.signum(vertical) : vertical) * (Double) this.minecraft.options.mouseWheelSensitivity().get();
            if (this.minecraft.getOverlay() == null && this.minecraft.screen == null && ShapeShifterCurseFabricClient.scrollActiveSkillKeybind.isDown()) {
                MagicGUIRender.onMouseScroll(d);
                ci.cancel();
            }
        }
    }
}

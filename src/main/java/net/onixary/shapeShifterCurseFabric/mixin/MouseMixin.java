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
    private void ssc$onMouseScroll(long handle, double xoffset, double yoffset, CallbackInfo ci) {
        if (handle == Minecraft.getInstance().getWindow().handle()) {
            double d = ((Boolean) this.minecraft.options.discreteMouseScroll().get() ? Math.signum(yoffset) : yoffset) * (Double) this.minecraft.options.mouseWheelSensitivity().get();
            if (this.minecraft.gui.overlay() == null && this.minecraft.gui.screen() == null && ShapeShifterCurseFabricClient.scrollActiveSkillKeybind.isDown()) {
                MagicGUIRender.onMouseScroll(d);
                ci.cancel();
            }
        }
    }
}

package net.onixary.shapeShifterCurseFabric.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.gui.render.state.pip.GuiEntityRenderState;
import net.onixary.shapeShifterCurseFabric.util.ClientUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiEntityRenderer.class)
public abstract class GuiEntityRendererMixin {
    @Inject(method = "renderToTexture", at = @At("HEAD"))
    private void ssc$pipPreviewStart(GuiEntityRenderState guiEntityRenderState, PoseStack poseStack, CallbackInfo ci) {
        ClientUtils.isOpenInventoryScreen = true;
    }

    @Inject(method = "renderToTexture", at = @At("RETURN"))
    private void ssc$pipPreviewEnd(GuiEntityRenderState guiEntityRenderState, PoseStack poseStack, CallbackInfo ci) {
        ClientUtils.isOpenInventoryScreen = false;
    }
}

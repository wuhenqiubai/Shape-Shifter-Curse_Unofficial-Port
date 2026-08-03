package net.onixary.shapeShifterCurseFabric.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.gui.render.state.pip.GuiEntityRenderState;
import net.onixary.shapeShifterCurseFabric.util.ClientUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 1.21.11 GUI 玩家模型预览走 Pictures-in-Picture（PIP）：InventoryScreen.renderEntityInInventoryFollowsMouse
 * 提交 GuiEntityRenderState，实际渲染延迟到 {@link GuiEntityRenderer#renderToTexture}（内部
 * entityRenderDispatcher.submit 同步执行 LivingEntityRenderer.submit → FormRenderFeature.submit → processAnimation）。
 * 在 renderToTexture 期间置位 ClientUtils.isOpenInventoryScreen，使：
 * - GUI 预览保留形态头部（processAnimation 不再误判为第一人称隐藏）；
 * - ExtraItemFeatureRenderer 不渲染 FERAL 第一人称物品（避免预览中跟随鼠标疯狂旋转）。
 */
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

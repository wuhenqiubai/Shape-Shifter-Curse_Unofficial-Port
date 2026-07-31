package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Safeguard: prevents NPE when EntityRenderDispatcher has a null renderer.
 * 1.21.11 渲染流程重构为 submit()/getRenderer(EntityRenderState)，旧的
 * @Redirect(EntityRenderer.render/shouldRender/getPackedLightCoords) 注入点已失效。
 * 若 1.21.11 的 getRenderer 返回 null 导致 NPE，需改注入 submit() 流程。
 */
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherSafetyMixin {
    // TODO: 1.21.11 渲染流程重构，此防护 mixin 的注入目标已失效，暂禁用。
    // 如需保留 null-renderer 防护，应改为注入 EntityRenderDispatcher.submit(...) 流程。
}

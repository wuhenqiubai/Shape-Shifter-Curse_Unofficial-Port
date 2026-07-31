package net.onixary.shapeShifterCurseFabric.mixin.render;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

/**
 * 1.21.11 渲染管线重构（RenderLayer.submit）：form overlay/emissive 纹理渲染
 * 已集成进 FormRenderFeature.rM_PartB（submit 模式，见 render/form_render/FormRenderFeature.java）。
 * 此 mixin 保留空壳以兼容 mixin json 配置。
 */
@Mixin(value = LivingEntityRenderer.class, priority = 10000)
public abstract class OverlayRenderMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
}

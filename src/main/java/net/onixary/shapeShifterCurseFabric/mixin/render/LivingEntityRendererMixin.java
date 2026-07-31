package net.onixary.shapeShifterCurseFabric.mixin.render;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

/**
 * 1.21.11 渲染管线重构（RenderLayer.submit）：原 rM_PartA/B（vanilla 部件隐藏 + overlay 纹理）
 * 与 EMF 暂停/恢复逻辑已全部集成进 FormRenderFeature.submit
 * （见 render/form_render/FormRenderFeature.java）。
 * 此 mixin 保留空壳以兼容 mixin json 配置。
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
}

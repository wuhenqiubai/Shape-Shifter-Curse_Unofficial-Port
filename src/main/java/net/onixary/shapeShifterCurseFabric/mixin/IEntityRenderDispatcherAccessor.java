package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityRenderDispatcher.class) // 指定目标类
public interface IEntityRenderDispatcherAccessor {
	// 1.21.11 字段改名：itemRenderer -> itemInHandRenderer，类型 ItemRenderer -> ItemInHandRenderer
	@Accessor("itemInHandRenderer") // 访问目标类中的字段
	ItemInHandRenderer getItemInHandRenderer();
}

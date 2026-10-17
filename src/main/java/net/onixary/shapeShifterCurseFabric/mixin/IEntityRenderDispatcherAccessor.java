package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityRenderDispatcher.class)
public interface IEntityRenderDispatcherAccessor {
	@Accessor("itemInHandRenderer")
	ItemInHandRenderer getItemInHandRenderer();
}

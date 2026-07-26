package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.onixary.shapeShifterCurseFabric.util.ModTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {
	@Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
	private void getMiningSpeedMixin(ItemStack stack, BlockState state, CallbackInfoReturnable<Float> cir) {
		if (state.is(ModTags.LIKE_COBWEB_TAG)) {
			cir.setReturnValue(15.0f);
		}
	}
}

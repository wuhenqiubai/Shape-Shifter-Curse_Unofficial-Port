package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.onixary.shapeShifterCurseFabric.util.ModTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShearsItem.class)
public class ShearsItemMixin {
	@Inject(method = "mineBlock", at = @At("RETURN"), cancellable = true)
	private void postMineMixin(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity miner, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValueZ() && state.is(ModTags.LIKE_COBWEB_TAG)) {
			cir.setReturnValue(true);
		}
	}

}

package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.additional_power.IsMorphScaleItemCondition;
import net.onixary.shapeShifterCurseFabric.items.RegCustomItem;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;
import net.onixary.shapeShifterCurseFabric.player_form.utils.TransformRelatedItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

	/**
	 * 注入到物品使用完成时的逻辑
	 *
	 * @param world 当前世界
	 * @param user  使用物品的实体（可能是玩家）
	 */
	@Inject(
			method = "finishUsing",
			at = @At("HEAD")
	)
	private void shape_shifter_curse$onFinishUsing(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
		// 仅在服务端且使用者为玩家时处理
		if (!world.isClient && user instanceof ServerPlayerEntity player) {
			ItemStack stack = (ItemStack) (Object) this;

			if (stack.getItem() == RegCustomItem.INHIBITOR) {
				TransformRelatedItems.OnUseCure(player, stack);
			} else if (stack.getItem() == RegCustomItem.POWERFUL_INHIBITOR) {
				TransformRelatedItems.OnUseCureFinal(player, stack);
			} else if (stack.getItem() == RegCustomItem.CREATIVE_INHIBITOR) {
				TransformRelatedItems.OnUseCreativeCure(player, stack);
			} else if (stack.getItem() == RegCustomItem.CATALYST) {
				TransformRelatedItems.OnUseCatalyst(player, stack);
			} else if (stack.getItem() == RegCustomItem.POWERFUL_CATALYST) {
				TransformRelatedItems.OnUsePowerfulCatalyst(player, stack);
			} else if (stack.getItem() == Items.GOLDEN_APPLE) {
				IForm currentForm = FormUtils.getPlayerForm(player);
				if (currentForm != null) {
					int currentFormIndex = currentForm.getFormTier();
					if (currentFormIndex == 0 || currentFormIndex == 1) {
						ShapeShifterCurseFabric.ON_USE_GOLDEN_APPLE.trigger(player);
					}
				}
			}
		}
	}

	@Inject(method = "getTooltip", at = @At("TAIL"))
	private void shape_shifter_curse$getTooltip(TooltipContext context, PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir) {
		ItemStack realThis = (ItemStack) (Object) this;
		List<Text> tooltip = cir.getReturnValue();
		var nbt = realThis.get(net.minecraft.component.DataComponentTypes.CUSTOM_DATA);
		if (nbt != null) {
			var compound = nbt.copyNbt();
			if (compound.contains("MorphScaleItem") && compound.getBoolean("MorphScaleItem")) {
				tooltip.add(Text.translatable("tooltip.shape_shifter_curse.morphscale_item").formatted(Formatting.GRAY));
			}
			if (compound.contains(IsMorphScaleItemCondition.IsMorphScaleFoodTagName) && compound.getBoolean(IsMorphScaleItemCondition.IsMorphScaleFoodTagName)) {
				tooltip.add(Text.translatable("tooltip.shape_shifter_curse.morphscale_food").formatted(Formatting.GRAY));
			}
		}
	}
}

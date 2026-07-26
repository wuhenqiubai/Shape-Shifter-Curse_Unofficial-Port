package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.additional_power.IsMorphScaleItemCondition;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;
import net.onixary.shapeShifterCurseFabric.status_effects.attachment.EffectManager;
import net.onixary.shapeShifterCurseFabric.status_effects.transformative_effects.TransformativeStatusInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Unique
    private static final List<TransformativeStatusInstance> tsiList = new ArrayList<>();

    @Inject(
            method = "finishUsingItem",
            at = @At("HEAD")
    )
    private void shape_shifter_curse$onFinishUsing(Level world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (!world.isClientSide && user instanceof ServerPlayer player) {
            ItemStack stack = (ItemStack) (Object) this;
            if(stack.getItem() == Items.GOLDEN_APPLE || stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE){
                IForm currentForm = FormUtils.getPlayerForm(player);
                if(!FormUtils.NoInstinct.hasFlag(currentForm) && !FormUtils.LockInstinct.hasFlag(currentForm)){
                    ShapeShifterCurseFabric.ON_USE_GOLDEN_APPLE.trigger(player);
                }
                if (EffectManager.hasTransformativeEffect(player)) {
                    player.sendSystemMessage(Component.translatable("info.shape-shifter-curse.transformative_effect_cure").withStyle(ChatFormatting.YELLOW));
                    EffectManager.clearTransformativeEffect(player);
                }
            }
            else if (stack.getItem() == Items.MILK_BUCKET) {
                if (EffectManager.hasTransformativeEffect(player)) {
                    player.sendSystemMessage(Component.translatable("info.shape-shifter-curse.milk_cannot_remove_effect").withStyle(ChatFormatting.YELLOW));
                    tsiList.clear();
                    Iterator<MobEffectInstance> iterator = player.getActiveEffects().iterator();
                    while (iterator.hasNext()) {
                        MobEffectInstance effectInstance = iterator.next();
                        if (effectInstance instanceof TransformativeStatusInstance tsi) {
                            tsiList.add(tsi);
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "finishUsingItem", at = @At("TAIL"))
    private void shape_shifter_curse$onFinishUsingEnd(Level world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (!world.isClientSide && user instanceof ServerPlayer player) {
            if (!tsiList.isEmpty()) {
                tsiList.forEach(tsi -> player.getActiveEffectsMap().put(tsi.getEffect(), tsi));
                tsiList.clear();
            }
        }
    }

	@Inject(method = "getTooltipLines", at = @At("TAIL"))
	private void shape_shifter_curse$getTooltip(TooltipContext context, Player player, TooltipFlag type, CallbackInfoReturnable<List<Component>> cir) {
		ItemStack realThis = (ItemStack) (Object) this;
		List<Component> tooltip = cir.getReturnValue();
		var nbt = realThis.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
		if (nbt != null) {
			var compound = nbt.copyTag();
			if (compound.contains("MorphScaleItem") && compound.getBoolean("MorphScaleItem")) {
				tooltip.add(Component.translatable("tooltip.shape_shifter_curse.morphscale_item").withStyle(ChatFormatting.GRAY));
			}
			if (compound.contains(IsMorphScaleItemCondition.IsMorphScaleFoodTagName) && compound.getBoolean(IsMorphScaleItemCondition.IsMorphScaleFoodTagName)) {
				tooltip.add(Component.translatable("tooltip.shape_shifter_curse.morphscale_food").withStyle(ChatFormatting.GRAY));
			}
		}
	}
}
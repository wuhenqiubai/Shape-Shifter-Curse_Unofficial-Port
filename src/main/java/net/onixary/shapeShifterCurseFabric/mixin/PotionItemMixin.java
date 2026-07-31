package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.data.CodexData;
import net.onixary.shapeShifterCurseFabric.items.RegCustomPotions;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.status_effects.CTPUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

// 1.21.11 PotionItem 不再覆写 finishUsingItem / appendHoverText（均继承自 Item），
// 因此改为混入 Item 基类，并在方法内用 instanceof PotionItem 守卫保持仅药水生效
@Mixin(Item.class)
public class PotionItemMixin {
    @Inject(method = "finishUsingItem", at = @At("HEAD"))
    public void finishUsing(ItemStack stack, Level world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (!(stack.getItem() instanceof PotionItem)) {
            return;
        }
        if (user instanceof Player player) {
            var nbt = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (nbt == null) return;
        Identifier CTPFormID = CTPUtils.getCTPFormIDFromNBT(nbt.copyTag());
            if (CTPFormID != null) {
                CTPUtils.setTransformativePotionForm(player, CTPFormID);
            }
        }
    }

    // 1.21.11 appendHoverText 签名改为 (ItemStack, Item.TooltipContext, TooltipDisplay, Consumer<Component>, TooltipFlag)
    @Inject(method = "appendHoverText", at = @At("RETURN"))
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag type, CallbackInfo ci) {
        if (!(stack.getItem() instanceof PotionItem)) {
            return;
        }
        var potionContents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        var potionEntry = potionContents.potion().orElse(null);
        if (potionEntry == null || potionEntry.value() != RegCustomPotions.CUSTOM_STATUE_FORM_POTION) {
            return;
        }
        var nbt = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (nbt == null) return;
        Identifier CTPFormID = CTPUtils.getCTPFormIDFromNBT(nbt.copyTag());
        if (CTPFormID != null) {
            Component formName = RegPlayerForms.getPlayerFormOrDefault(CTPFormID, RegPlayerForms.ORIGINAL_BEFORE_ENABLE).getContentText(CodexData.ContentType.NAME);
            consumer.accept(Component.translatable("tooltip.shape_shifter_curse.potion_target_form").append(formName));
        }
    }
}
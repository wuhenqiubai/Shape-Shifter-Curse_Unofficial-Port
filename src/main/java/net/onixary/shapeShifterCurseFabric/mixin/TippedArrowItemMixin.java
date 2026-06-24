package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TippedArrowItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.onixary.shapeShifterCurseFabric.items.RegCustomPotions;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.status_effects.CTPUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(TippedArrowItem.class)
public class TippedArrowItemMixin {
    @Inject(method = "appendTooltip", at = @At("RETURN"))
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type, CallbackInfo ci) {
        var potionContents = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
        var potionEntry = potionContents.potion().orElse(null);
        if (potionEntry == null || potionEntry.value() != RegCustomPotions.CUSTOM_STATUE_FORM_POTION) {
            return;
        }
        var nbt = stack.get(net.minecraft.component.DataComponentTypes.CUSTOM_DATA);
        if (nbt == null) return;
        Identifier CTPFormID = CTPUtils.getCTPFormIDFromNBT(nbt.copyNbt());
        if (CTPFormID != null) {
            Text formName = RegPlayerForms.getPlayerFormOrDefault(CTPFormID, RegPlayerForms.ORIGINAL_BEFORE_ENABLE).getFormName();
            tooltip.add(Text.translatable("tooltip.shape_shifter_curse.potion_target_form").append(formName));
        }
    }
}

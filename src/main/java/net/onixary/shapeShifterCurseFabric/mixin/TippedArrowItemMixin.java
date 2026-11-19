package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TippedArrowItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.onixary.shapeShifterCurseFabric.data.CodexData;
import net.onixary.shapeShifterCurseFabric.items.RegCustomPotions;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.status_effects.CTPUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(Item.class)
public class TippedArrowItemMixin {
    @Inject(method = "appendHoverText", at = @At("RETURN"))
    public void appendTooltip(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag, CallbackInfo ci) {
        if (!(itemStack.getItem() instanceof TippedArrowItem)) {
            return;
        }
        var potionContents = itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        var potionEntry = potionContents.potion().orElse(null);
        if (potionEntry == null || potionEntry.value() != RegCustomPotions.CUSTOM_STATUE_FORM_POTION) {
            return;
        }
        var nbt = itemStack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (nbt == null) return;
        Identifier CTPFormID = CTPUtils.getCTPFormIDFromNBT(nbt.copyTag());
        if (CTPFormID != null) {
            Component formName = RegPlayerForms.getPlayerFormOrDefault(CTPFormID, RegPlayerForms.ORIGINAL_BEFORE_ENABLE).getContentText(CodexData.ContentType.NAME);
            builder.accept(Component.translatable("tooltip.shape_shifter_curse.potion_target_form").append(formName));
        }
    }
}
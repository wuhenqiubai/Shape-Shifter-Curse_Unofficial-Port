package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.onixary.shapeShifterCurseFabric.status_effects.CTPUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Arrow.class)
public class ArrowEntityMixin {
    @Unique
    private boolean IsCTPArrow = false;

    @Unique
    private Identifier CTPFormID = null;

    @Inject(method = "setPickupItemStack", at = @At("HEAD"))
    public void setStack(ItemStack stack, CallbackInfo ci) {
        var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (customData == null) return;
        Identifier CTPFormID = CTPUtils.getCTPFormIDFromNBT(customData.copyTag());
        if (CTPFormID != null) {
            IsCTPArrow = true;
            this.CTPFormID = CTPFormID;
        }
    }

    @Inject(method = "doPostHurtEffects", at = @At("HEAD"))
    public void onHit(LivingEntity target, CallbackInfo ci) {
        if (IsCTPArrow && target instanceof Player player) {
            CTPUtils.setTransformativePotionForm(player, CTPFormID);
        }
    }

    @Inject(method = "getDefaultPickupItem", at = @At("RETURN"))
    public void getDefaultItemStack(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = cir.getReturnValue();
        if (IsCTPArrow && stack.getItem().equals(Items.TIPPED_ARROW)) {
            var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            if (customData != null) {
                CTPUtils.setCTPFormIDToNBT(customData.copyTag(), CTPFormID);
            }
        }
    }
}
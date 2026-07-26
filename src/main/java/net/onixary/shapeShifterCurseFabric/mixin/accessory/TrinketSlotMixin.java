package net.onixary.shapeShifterCurseFabric.mixin.accessory;

import dev.emi.trinkets.TrinketSlot;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.items.accessory.AccessoryItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TrinketSlot.class)
public interface TrinketSlotMixin {
    @Inject(method = "canInsert", at = @At("HEAD"), cancellable = true)
    private static void bypassValidatorForAccessoryItems(ItemStack stack, SlotReference slotRef, LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof AccessoryItem) {
            cir.setReturnValue(TrinketsApi.getTrinket(stack.getItem()).canEquip(stack, slotRef, entity));
        }
    }
}

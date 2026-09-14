package net.onixary.shapeShifterCurseFabric.mixin.accessory;

import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.callback.TrinketCallback;
import eu.pb4.trinkets.impl.slots.TrinketSlot;
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
    private static void bypassValidatorForAccessoryItems(ItemStack stack, TrinketSlotAccess slotRef, LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof AccessoryItem) {
            // 4.0: TrinketsApi.getTrinket(Item) 删除 -> TrinketCallback.getCallback(ItemStack)
            cir.setReturnValue(TrinketCallback.getCallback(stack).canEquip(stack, slotRef, entity));
        }
    }
}

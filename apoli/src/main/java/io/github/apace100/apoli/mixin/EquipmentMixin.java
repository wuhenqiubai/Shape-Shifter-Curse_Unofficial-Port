package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.RestrictArmorPower;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Equipable.class)
public interface EquipmentMixin {

    @Inject(method = "swapWithEquipmentSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"), cancellable = true)
    private void preventArmorEquipping(Item item, Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir, @Local ItemStack itemStack, @Local EquipmentSlot equipmentSlot) {
        PowerHolderComponent component = PowerHolderComponent.KEY.get(user);
        if(component.getPowers(RestrictArmorPower.class).stream().anyMatch(rap -> !rap.canEquip(itemStack, equipmentSlot))) {
            cir.setReturnValue(InteractionResultHolder.fail(itemStack));
        }
    }
}

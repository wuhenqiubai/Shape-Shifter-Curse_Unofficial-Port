package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PowerModifiedGrindstone;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyGrindstonePower;
import io.github.apace100.apoli.util.ApoliSharedMixinValues;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/world/inventory/GrindstoneMenu$3")
public class GrindstoneScreenHandlerBottomInputSlotMixin {
    @Unique private final GrindstoneMenu apoli$menu = ApoliSharedMixinValues.CURRENT_GRINDSTONE_MENU.get();

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void allowPowerStacks(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        PowerModifiedGrindstone pmg = (PowerModifiedGrindstone) apoli$menu;
        if(PowerHolderComponent.hasPower(pmg.getPlayer(), ModifyGrindstonePower.class, p -> p.allowsInBottom(stack))) {
            cir.setReturnValue(true);
        }
    }
}

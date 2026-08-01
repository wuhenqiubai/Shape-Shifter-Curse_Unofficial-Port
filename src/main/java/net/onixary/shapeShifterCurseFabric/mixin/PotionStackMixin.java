package net.onixary.shapeShifterCurseFabric.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.onixary.shapeShifterCurseFabric.additional_power.ModifyPotionStackPower;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Slot.class)
public abstract class PotionStackMixin {

    @Shadow @Final
    public Container container;

    /*
    @Redirect(
            method = "getMaxItemCount(Lnet/minecraft/item/ItemStack;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxCount()I")
    )
    private int modifyPotionStackSize(ItemStack itemStack) {
        if (this.inventory instanceof PlayerInventory) {
            PlayerEntity player = ((PlayerInventory) this.inventory).player;

            if (itemStack.getItem() instanceof PotionItem) {
                int StackCount = PowerHolderComponent.getPowers(player, ModifyPotionStackPower.class)
                        .stream()
                        .mapToInt(ModifyPotionStackPower::getCount)
                        .max().orElseGet(() -> 1);
                return Math.max(StackCount, itemStack.getMaxCount());
            }
        }

        return itemStack.getMaxCount();
    }
     */
    @Inject(method = "getMaxStackSize(Lnet/minecraft/world/item/ItemStack;)I", at=@At(value = "RETURN"), cancellable = true)
    private void modifyPotionStackSize(ItemStack itemStack, CallbackInfoReturnable<Integer> cir) {
        if (this.container instanceof Inventory) {
            Player player = ((Inventory) this.container).player;
            if (itemStack.getItem() instanceof PotionItem) {
                // PotionUtil removed in 1.21 → 从 DataComponent 读取药水类型
                PotionContents potionContents = itemStack.get(DataComponents.POTION_CONTENTS);
                boolean isWater = potionContents != null && potionContents.is(Potions.WATER);
                int StackCount = PowerHolderComponent.getPowers(player, ModifyPotionStackPower.class)
                        .stream()
                        .filter(power -> !power.isOnlyWaterPotion() || isWater)
                        .mapToInt(ModifyPotionStackPower::getCount)
		                .max().orElse(1);
                cir.setReturnValue(Math.max(StackCount, cir.getReturnValue()));
            }
        }
    }
}
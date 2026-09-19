package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ItemOnItemPower;
import io.github.apace100.apoli.power.ModifyFoodPower;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/InteractionResultHolder;pass(Ljava/lang/Object;)Lnet/minecraft/world/InteractionResultHolder;"), cancellable = true)
    private void tryItemAlwaysEdible(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemStack = user.getItemInHand(hand);
        if (PowerHolderComponent.KEY.get(user).getPowers(ModifyFoodPower.class).stream()
            .anyMatch(p -> p.doesMakeAlwaysEdible() && p.doesApply(itemStack))) {
            user.startUsingItem(hand);
            cir.setReturnValue(InteractionResult.CONSUME);
        }
    }

    @Inject(method = "overrideOtherStackedOnMe", at = @At("RETURN"), cancellable = true)
    private void forgeItem(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursorStackReference, CallbackInfoReturnable<Boolean> cir) {
        if(cir.getReturnValue()) {
            return;
        }
        if (clickType != ClickAction.SECONDARY) {
            return;
        }
        List<ItemOnItemPower> powers = PowerHolderComponent.getPowers(player, ItemOnItemPower.class).stream().filter(p -> p.doesApply(otherStack, stack)).collect(Collectors.toList());
        for (ItemOnItemPower p :
            powers) {
            p.execute(otherStack, stack, slot);
        }
        if(powers.size() > 0) {
            cir.setReturnValue(true);
        }
    }
}

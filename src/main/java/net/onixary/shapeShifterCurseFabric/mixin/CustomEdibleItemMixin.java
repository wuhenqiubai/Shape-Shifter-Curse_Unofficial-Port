package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.onixary.shapeShifterCurseFabric.util.CustomEdibleUtils.getPowerFoodComponent;

@Mixin(value = Item.class)
public abstract class CustomEdibleItemMixin {

    @Unique
    private static final ThreadLocal<Player> ssc$currentPlayer = new ThreadLocal<>();

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onUse(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = user.getItemInHand(hand);
        FoodProperties fc = getPowerFoodComponent(user, stack);
        if (fc != null) {
            if (user.canEat(fc.canAlwaysEat())) {
                ssc$currentPlayer.set(user);
                user.startUsingItem(hand);
                cir.setReturnValue(InteractionResult.CONSUME);
            } else {
                ssc$currentPlayer.remove();
                cir.setReturnValue(InteractionResult.FAIL);
            }
        }
    }

    @Inject(method = "finishUsingItem", at = @At("HEAD"), cancellable = true)
    private void onFinishUsing(ItemStack stack, Level world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (user instanceof Player player) {
            FoodProperties fc = getPowerFoodComponent(player, stack);
            if (fc != null) {
                cir.setReturnValue(player.eat(world, stack, fc));
            }
        }
    }

    @ModifyReturnValue(method = "getUseAnimation", at = @At("RETURN"))
    private ItemUseAnimation replaceUseAction(ItemUseAnimation original, ItemStack stack) {
        if (original == ItemUseAnimation.EAT) {
            return original;
        }
        Player player = ssc$currentPlayer.get();
        if (player != null && getPowerFoodComponent(player, stack) != null) {
            return ItemUseAnimation.EAT;
        }
        return original;
    }

    @ModifyReturnValue(method = "getEatingSound", at = @At("RETURN"))
    private SoundEvent replaceEatSound(SoundEvent original) {
        return ssc$currentPlayer.get() != null ? SoundEvents.GENERIC_EAT : original;
    }

    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    private void onGetMaxUseTime(ItemStack stack, LivingEntity user, CallbackInfoReturnable<Integer> cir) {
        if (user instanceof Player player) {
            FoodProperties fc = getPowerFoodComponent(player, stack);
            if (fc != null) {
                cir.setReturnValue(fc.eatSeconds() < 1.0f ? 16 : 32);
            }
        }
    }
}

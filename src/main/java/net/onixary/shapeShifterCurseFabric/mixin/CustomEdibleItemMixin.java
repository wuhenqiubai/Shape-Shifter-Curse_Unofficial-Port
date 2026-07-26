package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.onixary.shapeShifterCurseFabric.util.CustomEdibleUtils.getPowerFoodComponent;

@Mixin(value = Item.class)
public abstract class CustomEdibleItemMixin {

    @Unique
    private static final ThreadLocal<PlayerEntity> ssc$currentPlayer = new ThreadLocal<>();

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = user.getStackInHand(hand);
        FoodComponent fc = getPowerFoodComponent(user, stack);
        if (fc != null) {
            if (user.canConsume(fc.canAlwaysEat())) {
                ssc$currentPlayer.set(user);
                user.setCurrentHand(hand);
                cir.setReturnValue(TypedActionResult.consume(stack));
            } else {
                ssc$currentPlayer.remove();
                cir.setReturnValue(TypedActionResult.fail(stack));
            }
        }
    }

    @Inject(method = "finishUsing", at = @At("HEAD"), cancellable = true)
    private void onFinishUsing(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (user instanceof PlayerEntity player) {
            FoodComponent fc = getPowerFoodComponent(player, stack);
            if (fc != null) {
                cir.setReturnValue(player.eatFood(world, stack, fc));
            }
        }
    }

    @ModifyReturnValue(method = "getUseAction", at = @At("RETURN"))
    private UseAction replaceUseAction(UseAction original, ItemStack stack) {
        if (original == UseAction.EAT) {
            return original;
        }
        PlayerEntity player = ssc$currentPlayer.get();
        if (player != null && getPowerFoodComponent(player, stack) != null) {
            return UseAction.EAT;
        }
        return original;
    }

    @ModifyReturnValue(method = "getEatSound", at = @At("RETURN"))
    private SoundEvent replaceEatSound(SoundEvent original) {
        return ssc$currentPlayer.get() != null ? SoundEvents.ENTITY_GENERIC_EAT : original;
    }

    @Inject(method = "getMaxUseTime", at = @At("HEAD"), cancellable = true)
    private void onGetMaxUseTime(ItemStack stack, LivingEntity user, CallbackInfoReturnable<Integer> cir) {
        if (user instanceof PlayerEntity player) {
            FoodComponent fc = getPowerFoodComponent(player, stack);
            if (fc != null) {
                cir.setReturnValue(fc.eatSeconds() < 1.0f ? 16 : 32);
            }
        }
    }
}

package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static net.onixary.shapeShifterCurseFabric.util.CustomEdibleUtils.getPowerFoodComponent;

@Mixin(LivingEntity.class)
public class CustomEdiblePlayerAMixin {
    @Shadow
    protected ItemStack useItem;

    @ModifyExpressionValue(method = "onSyncedDataUpdated(Lnet/minecraft/network/syncher/EntityDataAccessor;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int onTrackedDataSet$getMaxUseTime(int original) {
        if ((Object)this instanceof Player playerEntity) {
            FoodProperties fc = getPowerFoodComponent(playerEntity, useItem);
            if (fc == null) {
                return original;
            }
            return fc.eatSeconds() < 1.0f ? 16 : 32;
        }
        return original;
    }

    @ModifyExpressionValue(method = "startUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int setCurrentHand$getMaxUseTime(int original) {
        if ((Object)this instanceof Player playerEntity) {
            FoodProperties fc = getPowerFoodComponent(playerEntity, useItem);
            if (fc == null) {
                return original;
            }
            return fc.eatSeconds() < 1.0f ? 16 : 32;
        }
        return original;
    }

    @ModifyExpressionValue(method = "triggerItemUseEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/UseAnim;"))
    private UseAnim spawnConsumptionEffects$getUseAction(UseAnim original, ItemStack stack, int particleCount) {
        if ((Object)this instanceof Player playerEntity) {
            FoodProperties fc = getPowerFoodComponent(playerEntity, useItem);
            if (fc == null) {
                return original;
            }
            return UseAnim.EAT;
        }
        return original;
    }
}

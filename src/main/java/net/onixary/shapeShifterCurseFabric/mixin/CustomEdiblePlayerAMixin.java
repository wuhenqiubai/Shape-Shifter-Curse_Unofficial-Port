package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
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
            return 32;
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
            return 32;
        }
        return original;
    }

    // TODO: 1.21.11 LivingEntity.triggerItemUseEffects 已被移除（粒子/声音逻辑迁入 Consumable.emitParticlesAndSounds，
    //  由 Consumable 记录的 animation 字段驱动，不再调用 ItemStack.getUseAnimation()）。
    //  自定义可食用物的 EAT 动画现由 CustomEdibleItemMixin.getUseAnimation 覆盖处理，此注入点暂时禁用。
    // @ModifyExpressionValue(method = "triggerItemUseEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/ItemUseAnimation;"))
    // private ItemUseAnimation spawnConsumptionEffects$getUseAction(ItemUseAnimation original, ItemStack stack, int particleCount) {
    //     if ((Object)this instanceof Player playerEntity) {
    //         FoodProperties fc = getPowerFoodComponent(playerEntity, useItem);
    //         if (fc == null) {
    //             return original;
    //         }
    //         return ItemUseAnimation.EAT;
    //     }
    //     return original;
    // }
}

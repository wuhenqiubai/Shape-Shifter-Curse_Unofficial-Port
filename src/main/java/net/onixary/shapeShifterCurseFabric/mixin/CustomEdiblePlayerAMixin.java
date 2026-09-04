package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static net.onixary.shapeShifterCurseFabric.util.CustomEdibleUtils.getPowerFoodComponent;

@Mixin(LivingEntity.class)
public class CustomEdiblePlayerAMixin {
    @Shadow
    protected ItemStack useItem;

    @ModifyExpressionValue(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/food/FoodProperties;)Lnet/minecraft/world/item/ItemStack;"))
    private boolean eat$isFood(boolean original, Level world, ItemStack stack) {
        // ShapeShifterCurseFabric.LOGGER.error("SSC_CE_SYSTEM_CEPA_01");
        if ((Object)this instanceof Player playerEntity) {
            return getPowerFoodComponent(playerEntity, stack) != null || original;
        }
        return original;
    }

    @ModifyExpressionValue(method = "shouldTriggerItemUseEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int shouldTriggerItemUseEffects$getUseDuration(int original) {
        // ShapeShifterCurseFabric.LOGGER.error("SSC_CE_SYSTEM_CEPA_04");
        if ((Object)this instanceof Player playerEntity) {
            FoodProperties fc = getPowerFoodComponent(playerEntity, useItem);
            if (fc == null) {
                return original;
            }
            return fc.eatSeconds() < 1.0f ? 16 : 32;
        }
        return original;
    }

    @ModifyExpressionValue(method = "onSyncedDataUpdated", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int onSyncedDataUpdated$getUseDuration(int original) {
        // ShapeShifterCurseFabric.LOGGER.error("SSC_CE_SYSTEM_CEPA_05");
        if ((Object)this instanceof Player playerEntity) {
            FoodProperties fc = getPowerFoodComponent(playerEntity, useItem);
            if (fc == null) {
                return original;
            }
            return fc.eatSeconds() < 1.0f ? 16 : 32;
        }
        return original;
    }

    // 和Lodestone冲突
    // 可惜协议是LGPL3的 我没法把有问题的代码复制出来解释
    // https://github.com/LodestarMC/Lodestone/blob/1.20.1-fabric/src/main/java/team/lodestar/lodestone/mixin/common/LivingEntityMixin.java
    // 中的lodestone$injectUseEvent会直接ci.cancel 我ModifyExpressionValue优先度没Inject高

    @ModifyExpressionValue(method = "startUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int startUsingItem$getUseDuration(int original) {
        // ShapeShifterCurseFabric.LOGGER.error("SSC_CE_SYSTEM_CEPA_06");
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
    private UseAnim triggerItemUseEffects$getUseAnimation(UseAnim original, ItemStack stack, int particleCount) {
        // ShapeShifterCurseFabric.LOGGER.error("SSC_CE_SYSTEM_CEPA_08");
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

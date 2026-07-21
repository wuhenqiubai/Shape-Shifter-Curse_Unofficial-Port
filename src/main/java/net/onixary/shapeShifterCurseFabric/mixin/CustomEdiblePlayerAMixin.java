package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.UseAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static net.onixary.shapeShifterCurseFabric.util.CustomEdibleUtils.getPowerFoodComponent;

@Mixin(LivingEntity.class)
public class CustomEdiblePlayerAMixin {
    @Shadow
    protected ItemStack activeItemStack;

    @ModifyExpressionValue(method = "onTrackedDataSet", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxUseTime(Lnet/minecraft/entity/LivingEntity;)I"))
    private int onTrackedDataSet$getMaxUseTime(int original) {
        if ((Object)this instanceof PlayerEntity playerEntity) {
            FoodComponent fc = getPowerFoodComponent(playerEntity, activeItemStack);
            if (fc == null) {
                return original;
            }
            return fc.eatSeconds() < 1.0f ? 16 : 32;
        }
        return original;
    }

    @ModifyExpressionValue(method = "setCurrentHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxUseTime(Lnet/minecraft/entity/LivingEntity;)I"))
    private int setCurrentHand$getMaxUseTime(int original) {
        if ((Object)this instanceof PlayerEntity playerEntity) {
            FoodComponent fc = getPowerFoodComponent(playerEntity, activeItemStack);
            if (fc == null) {
                return original;
            }
            return fc.eatSeconds() < 1.0f ? 16 : 32;
        }
        return original;
    }

    @ModifyExpressionValue(method = "spawnConsumptionEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getUseAction()Lnet/minecraft/util/UseAction;"))
    private UseAction spawnConsumptionEffects$getUseAction(UseAction original, ItemStack stack, int particleCount) {
        if ((Object)this instanceof PlayerEntity playerEntity) {
            FoodComponent fc = getPowerFoodComponent(playerEntity, activeItemStack);
            if (fc == null) {
                return original;
            }
            return UseAction.EAT;
        }
        return original;
    }
}

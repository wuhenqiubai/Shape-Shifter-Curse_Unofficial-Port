package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.access.ModifiableFoodEntity;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyFoodPower;
import io.github.apace100.apoli.util.ApoliSharedMixinValues;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(FoodProperties.class)
public class FoodPropertiesMixin {
    @WrapOperation(method = "onConsume", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/food/FoodProperties;)V"))
    private void apoli$storeSharedStack(FoodData instance, FoodProperties foodProperties, Operation<Void> original, @Local(argsOnly = true) ItemStack stack, @Local(argsOnly = true) LivingEntity entity) {
        List<ModifyFoodPower> mfps = PowerHolderComponent.getPowers(entity, ModifyFoodPower.class);
        mfps = mfps.stream().filter(mfp -> mfp.doesApply(stack)).toList();

        ApoliSharedMixinValues.CURRENT_STACK.set(stack);
        ((ModifiableFoodEntity) entity).setOriginalFoodStack(stack);
        ((ModifiableFoodEntity) entity).setCurrentModifyFoodPowers(mfps);
        original.call(instance, foodProperties);
        ApoliSharedMixinValues.CURRENT_STACK.remove();
        ((ModifiableFoodEntity) entity).setOriginalFoodStack(null);
        ((ModifiableFoodEntity) entity).setCurrentModifyFoodPowers(new ArrayList<>());
    }
}

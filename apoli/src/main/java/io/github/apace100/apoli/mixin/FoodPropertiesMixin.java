package io.github.apace100.apoli.mixin;

import net.minecraft.world.food.FoodProperties;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FoodProperties.class)
public class FoodPropertiesMixin {
    /*@WrapOperation(method = "onConsume", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/food/FoodProperties;)V"))
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
    }*/
}

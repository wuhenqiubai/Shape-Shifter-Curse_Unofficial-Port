package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.access.ModifiableFoodEntity;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyFoodPower;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(Consumable.class)
public class ConsumableMixin {
    @ModifyVariable(method = "onConsume", at = @At("HEAD"), argsOnly = true)
    private ItemStack modifyEatenItemStack(ItemStack original, @Local(argsOnly = true) LivingEntity entity) {
        if(entity instanceof Player) {
            return original;
        }
        List<ModifyFoodPower> mfps = PowerHolderComponent.getPowers(entity, ModifyFoodPower.class);
        mfps = mfps.stream().filter(mfp -> mfp.doesApply(original)).collect(Collectors.toList());
        ItemStack newStack = original;
        for(ModifyFoodPower mfp : mfps) {
            newStack = mfp.getConsumedItemStack(newStack);
        }
        ((ModifiableFoodEntity) entity).setCurrentModifyFoodPowers(mfps);
        ((ModifiableFoodEntity) entity).setOriginalFoodStack(original);
        return newStack;
    }

    @ModifyVariable(method = "onConsume", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;gameEvent(Lnet/minecraft/core/Holder;)V", shift = At.Shift.AFTER), argsOnly = true)
    private ItemStack unmodifyEatenItemStack(ItemStack modified, @Local(argsOnly = true) LivingEntity entity) {
        ModifiableFoodEntity foodEntity = (ModifiableFoodEntity) entity;
        ItemStack original = foodEntity.getOriginalFoodStack();
        if(original != null) {
            foodEntity.setOriginalFoodStack(null);
            return original;
        }
        return modified;
    }

    @Inject(method = "onConsume", at = @At("TAIL"))
    private void removeCurrentModifyFoodPowers(Level level, LivingEntity entity, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        ((ModifiableFoodEntity) entity).setCurrentModifyFoodPowers(new LinkedList<>());
    }

    @WrapOperation(method = "lambda$onConsume$1", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/consume_effects/ConsumeEffect;apply(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private static boolean preventApplyingFoodEffects(ConsumeEffect instance, Level level, ItemStack itemStack, LivingEntity entity, Operation<Boolean> original) {
        if (((ModifiableFoodEntity) entity).getCurrentModifyFoodPowers().stream().anyMatch(ModifyFoodPower::doesPreventEffects)) {
            return false;
        }

        return original.call(instance, level, itemStack, entity);
    }
}

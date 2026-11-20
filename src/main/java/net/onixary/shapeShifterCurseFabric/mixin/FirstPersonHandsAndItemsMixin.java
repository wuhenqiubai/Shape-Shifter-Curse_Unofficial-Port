package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static net.onixary.shapeShifterCurseFabric.util.CustomEdibleUtils.getPowerFoodComponent;

@Mixin(FirstPersonHandsAndItems.class)
public class FirstPersonHandsAndItemsMixin {
    @ModifyExpressionValue(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration(Lnet/minecraft/world/entity/LivingEntity;)I", ordinal = 0))
    private int ssc$mainHandUseDuration(int original) {
        var player = Minecraft.getInstance().player;
        return player != null ? ssc$foodUseDuration(original, player.getMainHandItem()) : original;
    }

    @ModifyExpressionValue(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration(Lnet/minecraft/world/entity/LivingEntity;)I", ordinal = 1))
    private int ssc$offHandUseDuration(int original) {
        var player = Minecraft.getInstance().player;
        return player != null ? ssc$foodUseDuration(original, player.getOffhandItem()) : original;
    }

    private static int ssc$foodUseDuration(int original, ItemStack stack) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return original;
        }
        FoodProperties fc = getPowerFoodComponent(player, stack);
        return fc != null ? 32 : original;
    }
}

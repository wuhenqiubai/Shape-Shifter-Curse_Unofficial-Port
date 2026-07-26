package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.onixary.shapeShifterCurseFabric.util.EnchantmentUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = AnvilMenu.class)
public class EnchantmentUtilAnvilMixin {
    @ModifyExpressionValue(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;canEnchant(Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean isAcceptableItem(boolean original, @Local(ordinal = 0) ItemStack itemStack, @Local Holder<Enchantment> enchantment) {
        if (!original) {
            return EnchantmentUtils.isItemCanEnchantment(enchantment.unwrapKey().orElseThrow(), itemStack);
        }
        return original;
    }
}

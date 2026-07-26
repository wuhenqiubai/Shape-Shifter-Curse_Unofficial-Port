package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.server.commands.EnchantCommand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.onixary.shapeShifterCurseFabric.util.EnchantmentUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantCommand.class)
public class EnchantmentUtilEnchantCommandMixin {
    @ModifyExpressionValue(method = "enchant", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;canEnchant(Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean isAcceptableItem(boolean original, @Local ItemStack itemStack, @Local(argsOnly = true) Holder<Enchantment> enchantment) {
        if (!original) {
            return EnchantmentUtils.isItemCanEnchantment(enchantment.unwrapKey().orElseThrow(), itemStack);
        }
        return original;
    }
}

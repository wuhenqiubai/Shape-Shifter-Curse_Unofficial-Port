package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.items.RegCustomItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(BowItem.class)
public abstract class CompoundBowMixin {
    @ModifyExpressionValue(method = "releaseUsing", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ArrowItem;createArrow(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/projectile/AbstractArrow;"))
    private AbstractArrow ssc$kineticArrow(AbstractArrow arrow, @Local(argsOnly = true) ItemStack bow) {
        if (bow.is(RegCustomItem.COMPOUND_KINETIC_BOW)) arrow.setBaseDamage(arrow.getBaseDamage() * 1.25);
        return arrow;
    }
}

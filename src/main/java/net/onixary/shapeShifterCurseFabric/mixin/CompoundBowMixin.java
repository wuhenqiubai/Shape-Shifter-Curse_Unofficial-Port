package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.items.RegCustomItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ProjectileWeaponItem.class)
public abstract class CompoundBowMixin {
    @Inject(method = "createProjectile", at = @At("RETURN"), cancellable = true)
    private void ssc$kineticArrow(Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammo, boolean crit,
                                  CallbackInfoReturnable<Projectile> cir) {
        if (weapon.is(RegCustomItem.COMPOUND_KINETIC_BOW) && cir.getReturnValue() instanceof AbstractArrow arrow) {
            AbstractArrowAccessor accessor = (AbstractArrowAccessor) arrow;
            accessor.ssc$setBaseDamage(accessor.ssc$getBaseDamage() * 1.25);
        }
    }
}

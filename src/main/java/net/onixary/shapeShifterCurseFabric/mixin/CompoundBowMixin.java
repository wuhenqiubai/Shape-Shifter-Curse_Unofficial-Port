package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.items.RegCustomItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 复合动力弓：射出时把箭的基伤 ×1.25。
 *
 * <p>⚠ 注入点是 {@code ProjectileWeaponItem#createProjectile}，不是看起来更顺的 {@code BowItem#releaseUsing}：
 * 1.21 的 releaseUsing 只做蓄力/音效/统计 → 转调 {@code shoot} → 再转调 {@code createProjectile}，
 * 而 {@code ArrowItem.createArrow} 只在 createProjectile 的方法体里被调用一次。
 * 挂在前两者属于「目标方法存在、但方法体里没有这个调用」，启动即 InjectionError。</p>
 *
 * <p>上游 Yarn 版挂的是 {@code BowItem.onStoppedUsing} + 3 参 {@code createArrow(World,ItemStack,LivingEntity)}，
 * 那是 1.20 的签名；1.21 起 createArrow 多了一个「发射武器 stack」参数，调用位置也搬到了 ProjectileWeaponItem。</p>
 *
 * <p>这里刻意用最朴素的 {@code @Inject(RETURN)} 而不是 {@code @ModifyExpressionValue} + {@code @Local}：
 * 后者在本注入点会报 "Scanned 0 target(s)"（回调里改用「取返回值就地改」即可，语义等价）。</p>
 *
 * <p>改挂 {@code ProjectileWeaponItem} 会连带覆盖弩，下面用 {@code is(COMPOUND_KINETIC_BOW)} 兜住。</p>
 */
@Mixin(ProjectileWeaponItem.class)
public abstract class CompoundBowMixin {
    @Inject(method = "createProjectile", at = @At("RETURN"), cancellable = true)
    private void ssc$kineticArrow(Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammo, boolean crit,
                                  CallbackInfoReturnable<Projectile> cir) {
        if (weapon.is(RegCustomItem.COMPOUND_KINETIC_BOW) && cir.getReturnValue() instanceof AbstractArrow arrow) {
            arrow.setBaseDamage(arrow.getBaseDamage() * 1.25);
        }
    }
}

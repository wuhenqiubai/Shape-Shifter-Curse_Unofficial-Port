package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownLingeringPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.onixary.shapeShifterCurseFabric.status_effects.CTPUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 1.21.11 恢复滞留药水 CTP 变身：1.21.1 注入 AbstractThrownPotion.makeAreaOfEffectCloud（已移除），
 * AreaEffectCloud 的创建移到 ThrownLingeringPotion.onHitAsPotion（addFreshEntity 处），
 * 子类方法需单独 @Mixin(ThrownLingeringPotion) 注入。
 */
@Mixin(ThrownLingeringPotion.class)
public abstract class ThrownLingeringPotionMixin {
    @Inject(
            method = "onHitAsPotion(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/phys/HitResult;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z")
    )
    private void ssc$applyLingeringPotion(ServerLevel serverLevel, ItemStack itemStack, HitResult hitResult, CallbackInfo ci, @Local AreaEffectCloud areaEffectCloud) {
        var customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            Identifier CTPFormID = CTPUtils.getCTPFormIDFromNBT(customData.copyTag());
            if (CTPFormID != null && areaEffectCloud instanceof CTPUtils.CTPFormIDHolder holder) {
                holder.setCTPFormID(CTPFormID);
            }
        }
    }
}

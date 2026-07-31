package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.onixary.shapeShifterCurseFabric.status_effects.CTPUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractThrownPotion.class)
public class PotionEntityMixin {
    // 1.21.11 AbstractThrownPotion.applySplash 已重构为 onHit(HitResult) 分发 → ThrownSplashPotion.onHitAsPotion，
    // 改为在 onHit(HEAD) 注入，仅当直接命中玩家且为 CTP 溅射药水时设置变身形态
    @Inject(method = "onHit(Lnet/minecraft/world/phys/HitResult;)V", at = @At("HEAD"))
    public void applySplashPotion(HitResult hitResult, CallbackInfo ci) {
        if (!((Object) this instanceof ThrownSplashPotion)) {
            return;
        }
        AbstractThrownPotion realThis = ((AbstractThrownPotion) (Object) this);
        if (realThis.level().isClientSide()) {
            return;
        }
        if (hitResult.getType() == HitResult.Type.ENTITY && ((EntityHitResult) hitResult).getEntity() instanceof Player player) {
            ItemStack stack = realThis.getItem();
            var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            if (customData != null) {
                Identifier CTPFormID = CTPUtils.getCTPFormIDFromNBT(customData.copyTag());
                if (CTPFormID != null) {
                    CTPUtils.setTransformativePotionForm(player, CTPFormID);
                }
            }
        }
    }

    // TODO: 1.21.11 makeAreaOfEffectCloud 已不存在，AreaEffectCloud 的创建移到 ThrownLingeringPotion.onHitAsPotion，
    // 本 mixin 目标是 AbstractThrownPotion，无法注入子类 ThrownLingeringPotion 的方法，暂时禁用。
    // 恢复该功能需要新增 ThrownLingeringPotion mixin（在 onHitAsPotion 的 addFreshEntity 处注入），
    // 或让 AreaEffectCloudEntityMixin 从 CUSTOM_DATA 组件读取 targetForm。
    // @Inject(method = "makeAreaOfEffectCloud", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    // public void applyLingeringPotion(PotionContents potion, CallbackInfo ci, @Local AreaEffectCloud areaEffectCloudEntity) {
    //     AbstractThrownPotion realThis = ((AbstractThrownPotion) (Object) this);
    //     ItemStack stack = realThis.getItem();
    //     var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
    //     if (customData != null) {
    //         Identifier CTPFormID = CTPUtils.getCTPFormIDFromNBT(customData.copyTag());
    //         if (CTPFormID != null && areaEffectCloudEntity instanceof CTPUtils.CTPFormIDHolder) {
    //             ((CTPUtils.CTPFormIDHolder) areaEffectCloudEntity).setCTPFormID(CTPFormID);
    //         }
    //     }
    // }
}

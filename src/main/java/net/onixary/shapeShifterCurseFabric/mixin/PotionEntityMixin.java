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

    //         if (CTPFormID != null && areaEffectCloudEntity instanceof CTPUtils.CTPFormIDHolder) {
    //             ((CTPUtils.CTPFormIDHolder) areaEffectCloudEntity).setCTPFormID(CTPFormID);
}

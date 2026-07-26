package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.onixary.shapeShifterCurseFabric.status_effects.CTPUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownPotion.class)
public class PotionEntityMixin {
    @Inject(method = "applySplash", at = @At("HEAD"))
    public void applySplashPotion(Iterable<MobEffectInstance> statusEffects, Entity entity, CallbackInfo ci) {
        if (entity instanceof Player player) {
            ThrownPotion realThis = ((ThrownPotion) (Object) this);
            ItemStack stack = realThis.getItem();
            var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            if (customData != null) {
                ResourceLocation CTPFormID = CTPUtils.getCTPFormIDFromNBT(customData.copyTag());
                if (CTPFormID != null) {
                    CTPUtils.setTransformativePotionForm(player, CTPFormID);
                }
            }
        }
    }

    @Inject(method = "makeAreaOfEffectCloud", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    public void applyLingeringPotion(PotionContents potion, CallbackInfo ci, @Local AreaEffectCloud areaEffectCloudEntity) {
        ThrownPotion realThis = ((ThrownPotion) (Object) this);
        ItemStack stack = realThis.getItem();
        var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (customData != null) {
            ResourceLocation CTPFormID = CTPUtils.getCTPFormIDFromNBT(customData.copyTag());
            if (CTPFormID != null && areaEffectCloudEntity instanceof CTPUtils.CTPFormIDHolder) {
                ((CTPUtils.CTPFormIDHolder) areaEffectCloudEntity).setCTPFormID(CTPFormID);
            }
        }
    }

}

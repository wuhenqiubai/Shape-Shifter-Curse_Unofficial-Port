package net.onixary.shapeShifterCurseFabric.mixin.projectile;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownLingeringPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.onixary.shapeShifterCurseFabric.additional_power.ActionOnSplashPotionTakeEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AbstractThrownPotion.class)
public class PotionEntityMixin {

    // 1.21.11 applyWater 重构为 private onHitAsWater(ServerLevel)
    @Inject(method = "onHitAsWater(Lnet/minecraft/server/level/ServerLevel;)V", at = @At("HEAD"))
    private void onApplyWater(ServerLevel serverLevel, CallbackInfo ci) {
        AbstractThrownPotion self = (AbstractThrownPotion) (Object) this;
        AABB box = self.getBoundingBox().inflate(4.0, 2.0, 4.0);

        List<LivingEntity> entities = self.level().getEntitiesOfClass(LivingEntity.class, box);
        for (LivingEntity entity : entities) {
            if (entity instanceof Player player) {
                double distance = self.distanceToSqr(entity);
                if (distance < 16.0) {
                    //ShapeShifterCurseFabric.LOGGER.info("Water bottle hit player {}, triggering action", player.getName().getString());
                    PowerHolderComponent.getPowers(player, ActionOnSplashPotionTakeEffect.class)
                            .stream()
                            .filter(ActionOnSplashPotionTakeEffect::shouldTriggerOnNoEffect)
                            .forEach(ActionOnSplashPotionTakeEffect::executeAction);
                }
            }
        }
    }

    // 1.21.11 applySplash 重构：改为注入 onHit(HEAD)，仅溅射药水生效
    @Inject(method = "onHit(Lnet/minecraft/world/phys/HitResult;)V", at = @At("HEAD"))
    private void onApplySplashPotion(HitResult hitResult, CallbackInfo ci) {
        if (!((Object) this instanceof ThrownSplashPotion)) {
            return;
        }
        AbstractThrownPotion self = (AbstractThrownPotion) (Object) this;
        if (self.level().isClientSide()) {
            return;
        }
        AABB box = self.getBoundingBox().inflate(4.0, 2.0, 4.0);
        List<LivingEntity> entities = self.level().getEntitiesOfClass(LivingEntity.class, box);

        for (LivingEntity livingEntity : entities) {
            double distance = self.distanceToSqr(livingEntity);
            if (distance < 16.0 && livingEntity instanceof Player player) {
                PowerHolderComponent.getPowers(player, ActionOnSplashPotionTakeEffect.class)
                        .stream()
                        .filter(ActionOnSplashPotionTakeEffect::isActive)
                        .forEach(ActionOnSplashPotionTakeEffect::executeAction);
            }
        }
    }

    // 1.21.11 makeAreaOfEffectCloud 重构：改为注入 onHit(HEAD)，仅滞留药水生效
    @Inject(method = "onHit(Lnet/minecraft/world/phys/HitResult;)V", at = @At("HEAD"))
    private void onApplyLingeringPotion(HitResult hitResult, CallbackInfo ci) {
        if (!((Object) this instanceof ThrownLingeringPotion)) {
            return;
        }
        AbstractThrownPotion self = (AbstractThrownPotion) (Object) this;
        if (self.level().isClientSide()) {
            return;
        }
        PotionContents contents = self.getItem().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        List<MobEffectInstance> effects = contents.customEffects();
        if (effects.isEmpty()) {
            AABB box = self.getBoundingBox().inflate(3.0, 2.0, 3.0);

            List<LivingEntity> entities = self.level().getEntitiesOfClass(LivingEntity.class, box);
            for (LivingEntity entity : entities) {
                if (entity instanceof Player player) {
                    PowerHolderComponent.getPowers(player, ActionOnSplashPotionTakeEffect.class)
                            .stream()
                            .filter(ActionOnSplashPotionTakeEffect::isActive)
                            .forEach(ActionOnSplashPotionTakeEffect::executeAction);
                }
            }
        }
    }
}

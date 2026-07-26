package net.onixary.shapeShifterCurseFabric.mixin.projectile;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.phys.AABB;
import net.onixary.shapeShifterCurseFabric.additional_power.ActionOnSplashPotionTakeEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ThrownPotion.class)
public class PotionEntityMixin {

    @Inject(method = "applyWater", at = @At("HEAD"))
    private void onApplyWater(CallbackInfo ci) {
        ThrownPotion self = (ThrownPotion) (Object) this;
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

    @Inject(method = "applySplash", at = @At("HEAD"))
    private void onApplySplashPotion(Iterable<MobEffectInstance> effects, Entity entity, CallbackInfo ci) {
        ThrownPotion self = (ThrownPotion) (Object) this;
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

    @Inject(method = "makeAreaOfEffectCloud", at = @At("HEAD"))
    private void onApplyLingeringPotion(PotionContents contents, CallbackInfo ci) {
        List<MobEffectInstance> effects = contents.customEffects();
        if (effects.isEmpty()) {
            ThrownPotion self = (ThrownPotion) (Object) this;
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
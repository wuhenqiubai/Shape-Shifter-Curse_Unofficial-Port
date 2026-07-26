package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.mixin.EntityAccessor;
import io.github.apace100.apoli.power.Power;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.additional_power.CustomWaterBreathingPower;
import net.onixary.shapeShifterCurseFabric.util.ModDamageSources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public final class CustomWaterBreathingMixin {

    @Mixin(LivingEntity.class)
    public static abstract class CanBreatheInWater extends Entity {

        public CanBreatheInWater(EntityType<?> type, Level world) {
            super(type, world);
        }

        @Inject(at = @At("HEAD"), method = "canBreatheUnderwater", cancellable = true)
        public void doWaterBreathing(CallbackInfoReturnable<Boolean> info) {
            if(PowerHolderComponent.getPowers(this, CustomWaterBreathingPower.class).stream().anyMatch(Power::isActive)) {
                info.setReturnValue(true);
            }
        }
    }


    @Mixin(Player.class)
    public static abstract class UpdateAir extends LivingEntity {
        @Shadow
        public abstract boolean isCreative();

        protected UpdateAir(EntityType<? extends LivingEntity> entityType, Level world) {
            super(entityType, world);
        }
        @Unique
        private int getNextAirUnderwaterSlow(int air, int waterBreathLevel) {
            if (waterBreathLevel >= 1000) {
                return air;
            }
            return waterBreathLevel > 0 && this.random.nextInt(waterBreathLevel + 1) > 0 ? air : air - 1;
        }

        // 使用原版水下呼吸逻辑的反向来实现陆地上慢速失去氧气
        // 水下呼吸等级越大，陆地上失去氧气的速度越慢
        // 24级为体验相对较好的数值
        @Inject(at = @At("TAIL"), method = "tick")
        private void tick(CallbackInfo info) {
            if (this.isCreative()) {
                if (this.getAirSupply() < this.getMaxAirSupply()) {
                    this.setAirSupply(this.getMaxAirSupply());
                }
                return; // 创造模式玩家不需要处理其他氧气逻辑
            }

            if(PowerHolderComponent.getPowers(this, CustomWaterBreathingPower.class).stream().anyMatch(Power::isActive)) {
                if(!this.isEyeInFluid(FluidTags.WATER)
                        && !this.hasEffect(MobEffects.WATER_BREATHING)
                        && !this.hasEffect(MobEffects.CONDUIT_POWER)) {
                    if(!((EntityAccessor) this).callIsBeingRainedOn()) {
                        int landWaterBreathLevel = PowerHolderComponent.getPowers(this, CustomWaterBreathingPower.class)
                                .stream()
                                .mapToInt(CustomWaterBreathingPower::getLandWaterBreathLevel).sum();

                        int landGain = this.increaseAirSupply(0);
                        this.setAirSupply(this.getNextAirUnderwaterSlow(this.getAirSupply(), landWaterBreathLevel) - landGain);
                    } else if(this.getAirSupply() < this.getMaxAirSupply()){
                        //int landGain = this.getNextAirOnLand(0);
                        //this.setAir(this.getAir() - landGain);
                        this.setAirSupply(this.increaseAirSupply(this.getAirSupply()));
                    }
                } else if(this.getAirSupply() < this.getMaxAirSupply()){
                    this.setAirSupply(this.increaseAirSupply(this.getAirSupply()));
                }

                boolean isDamageWhenNoAir = PowerHolderComponent.getPowers(this, CustomWaterBreathingPower.class)
                        .stream()
                        .anyMatch(CustomWaterBreathingPower::isDamage_when_no_air);

                if(isDamageWhenNoAir)
                {
                    // 正常造成溺水伤害
                    if (this.getAirSupply() == -20) {
                        this.setAirSupply(0);

                        for(int i = 0; i < 8; ++i) {
                            double f = this.random.nextDouble() - this.random.nextDouble();
                            double g = this.random.nextDouble() - this.random.nextDouble();
                            double h = this.random.nextDouble() - this.random.nextDouble();
                            this.level().addParticle(ParticleTypes.BUBBLE, this.getRandomX(0.5), this.getEyeY() + this.random.nextGaussian() * 0.08D, this.getRandomZ(0.5), f * 0.5F, g * 0.5F + 0.25F, h * 0.5F);
                        }

                        this.hurt(ModDamageSources.getSource(damageSources(), ModDamageSources.NO_WATER_FOR_GILLS), 2.0F);
                    }
                }
                else{
                    // 不造成溺水伤害
                    if (this.getAirSupply() < 0) {
                        // 没有氧气（湿润度）时设为-1定值来便于判定
                        this.setAirSupply(-1);
                    }
                }

            }
        }

        @ModifyExpressionValue(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"), method = "turtleHelmetTick")
        public boolean isSubmergedInProxy(boolean submerged) {
            if(PowerHolderComponent.getPowers(this, CustomWaterBreathingPower.class).stream().anyMatch(Power::isActive)) {
                return !submerged;
            }
            return submerged;
        }
    }
}

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

        // 原版 baseTick 在「非眼浸水且 air < max」时执行 setAirSupply(increaseAirSupply(air)) 即每 tick +4。
        // 而本 mixin 的 UpdateAir.tick（Player.tick 的 TAIL）要在干燥陆地自行接管氧气（按 landWaterBreathLevel
        // 决定掉氧速度），两者叠加会互相拉锯：air=300 时 baseTick 不加（300 不 < 300），TAIL 却照样 -4，
        // 于是稳态在 296<->300 每 tick 摆动，SynchedEntityData 每 tick 标脏发包 → 氧气 UI 一直闪。
        // 这里把 baseTick 的那次 +4 抵消成恒等，让 SSC 独占干燥陆地的氧气管理。
        // 只匹配 LivingEntity.baseTick 中唯一一处 increaseAirSupply 调用（:430）；decreaseAirSupply 不受影响。
        @ModifyExpressionValue(
            method = "baseTick",
            require = 1,
            at = @At(value = "INVOKE",
                target = "Lnet/minecraft/world/entity/LivingEntity;increaseAirSupply(I)I")
        )
        private int ssc$suppressVanillaLandRefill(int original) {
            LivingEntity self = (LivingEntity)(Object)this;
            if(!PowerHolderComponent.getPowers(self, CustomWaterBreathingPower.class).stream().anyMatch(Power::isActive)) {
                return original; // 非 SSC 形态：完全保持原版行为
            }
            // 与 UpdateAir.tick 的「干燥陆地」分支条件严格镜像；只有这些情况下才由 SSC 全权决定 air
            if(!self.isEyeInFluid(FluidTags.WATER)
                    && !self.hasEffect(MobEffects.WATER_BREATHING)
                    && !self.hasEffect(MobEffects.CONDUIT_POWER)
                    && !((EntityAccessor) self).callIsInRain()) {
                return self.getAirSupply(); // 恒等：让 baseTick 那次 setAirSupply 不改变数值
            }
            return original; // 水下 / 雨天 / 有潮涌或水肺：保留原版恢复
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
                    if(!((EntityAccessor) this).callIsInRain()) {
                        int landWaterBreathLevel = PowerHolderComponent.getPowers(this, CustomWaterBreathingPower.class)
                                .stream()
                                .mapToInt(CustomWaterBreathingPower::getLandWaterBreathLevel).sum();

                        // baseTick 的 +4 已在 ssc$suppressVanillaLandRefill 中抵消为恒等，
                        // 故这里直接套用「陆地掉氧」公式，不再手动 landGain -4
                        // （否则 air 已满时 baseTick 不加而这里照减，会导致 296<->300 振荡）。
                        this.setAirSupply(this.getNextAirUnderwaterSlow(this.getAirSupply(), landWaterBreathLevel));
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

        // 1.21.11 把乌龟头盔的 isEyeInFluid 判断从 turtleHelmetTick() 移到了调用方 tick()（见 Player.tick 的 line 284），
        // 故注入点需改为 method = "tick"，否则 @ModifyExpressionValue 找不到 INVOKE target（静默失效）。
        @ModifyExpressionValue(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"), method = "tick")
        public boolean isSubmergedInProxy(boolean submerged) {
            if(PowerHolderComponent.getPowers(this, CustomWaterBreathingPower.class).stream().anyMatch(Power::isActive)) {
                return !submerged;
            }
            return submerged;
        }
    }
}

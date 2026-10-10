package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.onixary.shapeShifterCurseFabric.additional_power.WitherIntervalPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MobEffectInstance.class)
public abstract class WitherIntervalMixin {
    // ⚠ 上游 Yarn 写的是 `update`，但 Mojmap 的 `update(MobEffectInstance)` 是「合并隐藏效果」那个 1 参重载，
    //   真正调 shouldApplyEffectTickThisTick 的是 `tick(LivingEntity, Runnable)`（= Yarn 的 update(Entity,Runnable)）。
    //   挂错方法的后果是注入点找不到 —— 要么启动崩，要么（require 宽松时）整个 mixin 静默失效。
    @Redirect(method = "tick(Lnet/minecraft/world/entity/LivingEntity;Ljava/lang/Runnable;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffect;shouldApplyEffectTickThisTick(II)Z"))
    private boolean ssc$witherInterval(MobEffect effect, int duration, int amplifier, LivingEntity entity, Runnable callback) {
        boolean vanilla = effect.shouldApplyEffectTickThisTick(duration, amplifier);
        // ⚠ effect 是裸 MobEffect，MobEffects.WITHER 是 Holder —— 直接 `==` 恒为 false，等同整个 mixin 失效。
        return effect == MobEffects.WITHER.value() ? WitherIntervalPower.shouldApply(entity, duration, amplifier, vanilla) : vanilla;
    }
}

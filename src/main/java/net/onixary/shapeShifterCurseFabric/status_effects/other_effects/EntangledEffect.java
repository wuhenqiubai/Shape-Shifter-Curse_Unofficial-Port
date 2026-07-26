package net.onixary.shapeShifterCurseFabric.status_effects.other_effects;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.onixary.shapeShifterCurseFabric.status_effects.EntangledEffectUtils;
import net.onixary.shapeShifterCurseFabric.status_effects.RegOtherStatusEffects;

public class EntangledEffect extends MobEffect {
    public EntangledEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration >= 1;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        Holder<MobEffect> entangled = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(RegOtherStatusEffects.ENTANGLED_EFFECT);
        MobEffectInstance instance = entity.getEffect(entangled);
        if (instance != null) {
            int NowDuration = instance.getDuration();
            int CurrentLevel = instance.getAmplifier();
            int TargetLevel = NowDuration / EntangledEffectUtils.ENTANGLED_DURATION_PER_LEVEL;
            if (CurrentLevel != TargetLevel) {
                entity.removeEffect(entangled);
                entity.addEffect(new MobEffectInstance(entangled, NowDuration, TargetLevel));
            }
        }
        return true;
    }
}

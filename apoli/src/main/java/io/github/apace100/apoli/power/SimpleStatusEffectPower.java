package io.github.apace100.apoli.power;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class SimpleStatusEffectPower extends StatusEffectPower {
    public SimpleStatusEffectPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }

    public SimpleStatusEffectPower(PowerType<?> type, LivingEntity entity, MobEffectInstance effectInstance) {
        super(type, entity, effectInstance);
    }
}

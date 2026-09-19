package io.github.apace100.apoli.power;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.LinkedList;
import java.util.List;

public class StatusEffectPower extends Power {

    protected final List<MobEffectInstance> effects = new LinkedList<>();

    public StatusEffectPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }
    public StatusEffectPower(PowerType<?> type, LivingEntity entity, MobEffectInstance effectInstance) {
        super(type, entity);
        addEffect(effectInstance);
    }

    public StatusEffectPower addEffect(Holder<MobEffect> effect) {
        return addEffect(effect, 80);
    }

    public StatusEffectPower addEffect(Holder<MobEffect> effect, int lingerDuration) {
        return addEffect(effect, lingerDuration, 0);
    }

    public StatusEffectPower addEffect(Holder<MobEffect> effect, int lingerDuration, int amplifier) {
        return addEffect(new MobEffectInstance(effect, lingerDuration, amplifier));
    }

    public StatusEffectPower addEffect(MobEffectInstance instance) {
        effects.add(instance);
        return this;
    }

    public void applyEffects() {
        effects.stream().map(MobEffectInstance::new).forEach(entity::addEffect);
    }
}

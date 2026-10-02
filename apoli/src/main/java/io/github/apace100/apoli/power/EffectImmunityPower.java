package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashSet;
import java.util.List;

public class EffectImmunityPower extends Power {

    protected final HashSet<MobEffect> effects = new HashSet<>();
    private final boolean inverted;

    public EffectImmunityPower(PowerType<?> type, LivingEntity entity, boolean inverted) {
        super(type, entity);
        this.inverted = inverted;
    }

    public EffectImmunityPower addEffect(MobEffect effect) {
        effects.add(effect);
        return this;
    }

    public boolean doesApply(MobEffectInstance instance) {
        return doesApply(instance.getEffect().value());
    }

    public boolean doesApply(MobEffect effect) {
        return inverted ^ effects.contains(effect);
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("effect_immunity"),
            new SerializableData()
                .add("effect", SerializableDataTypes.STATUS_EFFECT, null)
                .add("effects", SerializableDataTypes.STATUS_EFFECTS, null)
                .add("inverted", SerializableDataTypes.BOOLEAN, false),
            data ->
                (type, player) -> {
                    EffectImmunityPower power = new EffectImmunityPower(type, player, data.get("inverted"));
                    if(data.isPresent("effect")) {
                        power.addEffect(data.get("effect"));
                    }
                    if(data.isPresent("effects")) {
                        ((List<MobEffect>)data.get("effects")).forEach(power::addEffect);
                    }
                    return power;
                })
            .allowCondition();
    }
}

package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import java.util.ArrayList;
import java.util.List;

public class ApplyEffectPower extends Power {

    private final List<MobEffectInstance> effects;
    private final List<MobEffectInstance> storeEffects;
    private boolean isApplied = false;

    public ApplyEffectPower(PowerType<?> type, LivingEntity entity, List<MobEffectInstance> effects) {
        super(type, entity);
        if (effects == null) {
            effects = new ArrayList<>();
        }
        this.effects = effects;
        this.storeEffects = new ArrayList<>();
        this.setTicking(true);
    }

    @Override
    public void tick() {
        if (this.isActive() && !this.isApplied) {
            this.ApplyEffects();
            this.isApplied = true;
        } else if (!this.isActive() && this.isApplied) {
            this.RemoveEffects();
            this.isApplied = false;
        }
        if (entity.tickCount % 20 == 0 && this.isActive() && this.isApplied) {
            this.checkEffects();
        }
    }

    private void ApplyEffects() {
        for (MobEffectInstance effect : this.effects) {
            if (this.entity.hasEffect(effect.getEffect())) {
                this.storeEffects.add(this.entity.getEffect(effect.getEffect()));
                this.entity.removeEffect(effect.getEffect());
            }
            this.entity.addEffect(new MobEffectInstance(effect));
        }
    }

    private void checkEffects() {
        for (MobEffectInstance effect : this.effects) {
            if (!this.entity.hasEffect(effect.getEffect())) {
                this.entity.addEffect(new MobEffectInstance(effect));
            }
        }
    }

    private void RemoveEffects() {
        for (MobEffectInstance effect : this.effects) {
            this.entity.removeEffect(effect.getEffect());
        }
        for (MobEffectInstance effect : this.storeEffects) {
            this.entity.addEffect(effect);
        }
        this.storeEffects.clear();
    }

    public void onRemoved() {
        if (this.isApplied) {
            this.RemoveEffects();
        }
    }

    public static PowerFactory<?> createFactory() {
        return new PowerFactory<>(
                ShapeShifterCurseFabric.identifier("apply_effect"),
                new SerializableData()
                        .add("status_effects", SerializableDataTypes.STATUS_EFFECT_INSTANCES, null),  // 时效必须为无限
                data -> (powerType, entity) -> new ApplyEffectPower(powerType, entity, data.get("status_effects"))
        ).allowCondition();
    }
}

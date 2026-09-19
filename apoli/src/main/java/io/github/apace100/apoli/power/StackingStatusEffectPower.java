package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class StackingStatusEffectPower extends StatusEffectPower {

    private final int minStack;
    private final int maxStack;
    private final int durationPerStack;
    private final int tickRate;

    private int currentStack;

    public StackingStatusEffectPower(PowerType<?> type, LivingEntity entity, int minStack, int maxStack, int durationPerStack, int tickRate) {
        super(type, entity);
        this.minStack = minStack;
        this.maxStack = maxStack;
        this.durationPerStack = durationPerStack;
        this.tickRate = tickRate;
        this.setTicking(true);
    }

    public void tick() {
        if(entity.tickCount % tickRate == 0) {
            if(isActive()) {
                currentStack += 1;
                if(currentStack > maxStack) {
                    currentStack = maxStack;
                }
                if(currentStack > 0) {
                    applyEffects();
                }
            } else {
                currentStack -= 1;
                if(currentStack < minStack) {
                    currentStack = minStack;
                }
            }
        }
    }

    @Override
    public void applyEffects() {
        effects.forEach(sei -> {
            int duration = durationPerStack * currentStack;
            if(duration > 0) {
                MobEffectInstance applySei = new MobEffectInstance(sei.getEffect(), duration, sei.getAmplifier(), sei.isAmbient(), sei.isVisible(), sei.showIcon());
                entity.addEffect(applySei);
            }
        });
    }

    @Override
    public Tag toTag(HolderLookup.Provider provider) {
        return IntTag.valueOf(currentStack);
    }

    @Override
    public void fromTag(Tag tag, HolderLookup.Provider provider) {
        currentStack = ((IntTag)tag).getAsInt();
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("stacking_status_effect"),
            new SerializableData()
                .add("min_stacks", SerializableDataTypes.INT)
                .add("max_stacks", SerializableDataTypes.INT)
                .add("duration_per_stack", SerializableDataTypes.INT)
                .add("tick_rate", SerializableDataTypes.INT, 10)
                .add("effect", SerializableDataTypes.STATUS_EFFECT_INSTANCE, null)
                .add("effects", SerializableDataTypes.STATUS_EFFECT_INSTANCES, null),
            data ->
                (type, player) -> {
                    StackingStatusEffectPower power = new StackingStatusEffectPower(type, player,
                        data.getInt("min_stacks"),
                        data.getInt("max_stacks"),
                        data.getInt("duration_per_stack"),
                        data.getInt("tick_rate"));
                    if(data.isPresent("effect")) {
                        power.addEffect((MobEffectInstance)data.get("effect"));
                    }
                    if(data.isPresent("effects")) {
                        ((List<MobEffectInstance>)data.get("effects")).forEach(power::addEffect);
                    }
                    return power;
                })
            .allowCondition();
    }
}

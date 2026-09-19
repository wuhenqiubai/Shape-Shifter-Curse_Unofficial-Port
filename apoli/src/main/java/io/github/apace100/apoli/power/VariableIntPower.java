package io.github.apace100.apoli.power;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;

public class VariableIntPower extends Power {

    protected final int min, max;
    protected int currentValue;

    public VariableIntPower(PowerType<?> type, LivingEntity entity, int startValue) {
        this(type, entity, startValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public VariableIntPower(PowerType<?> type, LivingEntity entity, int startValue, int min, int max) {
        super(type, entity);
        this.currentValue = startValue;
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getValue() {
        return currentValue;
    }

    public int setValue(int newValue) {
        if(newValue > getMax())
            newValue = getMax();
        if(newValue < getMin())
            newValue = getMin();
        return currentValue = newValue;
    }

    public int increment() {
        return setValue(getValue() + 1);
    }

    public int decrement() {
        return setValue(getValue() - 1);
    }

    @Override
    public Tag toTag(HolderLookup.Provider provider) {
        return IntTag.valueOf(currentValue);
    }

    @Override
    public void fromTag(Tag tag, HolderLookup.Provider provider) {
        currentValue = ((IntTag)tag).getAsInt();
    }
}

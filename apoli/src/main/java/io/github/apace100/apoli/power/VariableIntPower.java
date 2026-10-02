package io.github.apace100.apoli.power;

import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
    public void toValue(ValueOutput output) {
        output.putInt("CurrentValue", currentValue);
    }

    @Override
    public void fromValue(ValueInput input) {
        currentValue = input.getIntOr("CurrentValue", 0);
    }

    @Override
    public Tag toTag() {
        return IntTag.valueOf(currentValue);
    }

    @Override
    public void fromTag(Tag input) {
        currentValue = ((IntTag) input).intValue();
    }
}

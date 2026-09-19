package io.github.apace100.apoli.util;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.Comparator;
import java.util.List;

public final class AttributeUtil {

    public static void sortModifiers(List<AttributeModifier> modifiers) {
        modifiers.sort(Comparator.comparing(AttributeModifier::operation));
    }

    public static double sortAndApplyModifiers(List<AttributeModifier> modifiers, double baseValue) {
        sortModifiers(modifiers);
        return applyModifiers(modifiers, baseValue);
    }

    public static double applyModifiers(List<AttributeModifier> modifiers, double baseValue) {
        double currentValue = baseValue;
        if(modifiers != null) {
            for(AttributeModifier modifier : modifiers) {
                switch(modifier.operation()) {
                    case ADD_VALUE:
                        currentValue += modifier.amount();
                        break;
                    case ADD_MULTIPLIED_BASE:
                        currentValue += baseValue * modifier.amount();
                        break;
                    case ADD_MULTIPLIED_TOTAL:
                        currentValue *= (1 + modifier.amount());
                        break;
                }
            }
        }
        return currentValue;
    }
}

package io.github.apace100.apoli.power;

import net.minecraft.world.entity.LivingEntity;

public class ModifyHealingPower extends ValueModifyingPower {
    public ModifyHealingPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }
}

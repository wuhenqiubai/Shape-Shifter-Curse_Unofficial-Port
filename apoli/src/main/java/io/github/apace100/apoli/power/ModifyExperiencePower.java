package io.github.apace100.apoli.power;

import net.minecraft.world.entity.LivingEntity;

public class ModifyExperiencePower extends ValueModifyingPower {

    public ModifyExperiencePower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }
}

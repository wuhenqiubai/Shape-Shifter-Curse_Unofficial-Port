package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;

import java.util.Optional;

public class ModifyCameraSubmersionTypePower extends Power {

    private final Optional<FogType> from;
    private final FogType to;

    public ModifyCameraSubmersionTypePower(PowerType<?> type, LivingEntity entity, Optional<FogType> from, FogType to) {
        super(type, entity);
        this.from = from;
        this.to = to;
    }

    public boolean doesModify(FogType original) {
        return from.isEmpty() || from.get() == original;
    }

    public FogType getNewType() {
        return to;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("modify_camera_submersion"),
            new SerializableData()
                .add("from", SerializableDataTypes.CAMERA_SUBMERSION_TYPE, null)
                .add("to", SerializableDataTypes.CAMERA_SUBMERSION_TYPE),
            data ->
                (type, player) -> new ModifyCameraSubmersionTypePower(type, player,
                    data.isPresent("from") ? Optional.of((FogType)data.get("from")) : Optional.empty(),
                    (FogType)data.get("to")))
            .allowCondition();
    }
}

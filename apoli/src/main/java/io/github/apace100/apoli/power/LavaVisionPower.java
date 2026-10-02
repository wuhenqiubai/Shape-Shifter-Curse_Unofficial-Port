package io.github.apace100.apoli.power;

import de.dafuqs.additionalentityattributes.AdditionalEntityAttributes;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class LavaVisionPower extends AttributePower {

    private final float s;
    private final float v;

    public LavaVisionPower(PowerType<?> type, LivingEntity entity, float s, float v) {
        super(type, entity, false);
        addModifier(AdditionalEntityAttributes.LAVA_VISIBILITY, new AttributeModifier(Apoli.identifier("lava_vision"), v - 1, AttributeModifier.Operation.ADD_VALUE));
        this.s = s;
        this.v = v;
    }

    public float getS() {
        return s;
    }

    public float getV() {
        return v;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("lava_vision"),
            new SerializableData()
                .add("s", SerializableDataTypes.FLOAT)
                .add("v", SerializableDataTypes.FLOAT),
            data ->
                (type, player) ->
                    new LavaVisionPower(type, player, data.getFloat("s"), data.getFloat("v")))
            .allowCondition();
    }
}

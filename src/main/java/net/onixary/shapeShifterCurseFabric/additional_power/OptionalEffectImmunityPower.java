package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.power.EffectImmunityPower;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class OptionalEffectImmunityPower {

    public static @Nullable MobEffect getStatusEffect(ResourceLocation effectID) {
        Optional<MobEffect> result = BuiltInRegistries.MOB_EFFECT.getOptional(effectID);
        return result.orElse(null);
    }

    public static PowerFactory<?> createFactory() {
        return new PowerFactory<>(
                ShapeShifterCurseFabric.identifier("optional_effect_immunity"),
                new SerializableData()
                        .add("effect", SerializableDataTypes.IDENTIFIER, null)
                        .add("effects", SerializableDataTypes.IDENTIFIERS, null)
                        .add("inverted", SerializableDataTypes.BOOLEAN, false),
                (data) -> (type, player) -> {
                    EffectImmunityPower power = new EffectImmunityPower(type, player, data.get("inverted"));
                    if (data.isPresent("effect")) {
                        MobEffect effect = getStatusEffect(data.get("effect"));
                        if (effect != null) {
                            power.addEffect(effect);
                        }
                    }
                    if (data.isPresent("effects")) {
                        List<ResourceLocation> effectIDs = data.get("effects");
                        for (ResourceLocation effectID : effectIDs) {
                            MobEffect effect = getStatusEffect(effectID);
                            if (effect != null) {
                                power.addEffect(effect);
                            }
                        }
                    }
                    return power;
                }
        ).allowCondition();
    }
}

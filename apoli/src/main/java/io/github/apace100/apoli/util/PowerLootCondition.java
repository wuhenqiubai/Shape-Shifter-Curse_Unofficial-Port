package io.github.apace100.apoli.util;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

public class PowerLootCondition implements LootItemCondition {
    public static final MapCodec<PowerLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Identifier.CODEC
                .fieldOf("power")
                .forGetter(c -> c.powerId),
            Identifier.CODEC
                .optionalFieldOf("source", null)
                .forGetter(c -> c.powerSourceId)
        )
            .apply(instance, PowerLootCondition::new)
    );
    private final Identifier powerId;
    private final Identifier powerSourceId;

    private PowerLootCondition(Identifier powerId, Identifier powerSourceId) {
        this.powerId = powerId;
        this.powerSourceId = powerSourceId;
    }

    @Override
    public MapCodec<? extends LootItemCondition> codec() {
        return CODEC;
    }

    public boolean test(LootContext lootContext) {

        Optional<PowerHolderComponent> optionalPowerHolderComponent = PowerHolderComponent.KEY.maybeGet(
            lootContext.getOptionalParameter(LootContextParams.THIS_ENTITY)
        );

        if (optionalPowerHolderComponent.isPresent()) {

            PowerHolderComponent powerHolderComponent = optionalPowerHolderComponent.get();
            PowerType<?> powerType = PowerTypeRegistry.get(powerId);

            if (powerSourceId != null) return powerHolderComponent.hasPower(powerType, powerSourceId);
            else return powerHolderComponent.hasPower(powerType);

        }

        return false;
    }

}

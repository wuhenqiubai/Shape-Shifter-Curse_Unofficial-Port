package io.github.apace100.apoli.util;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import java.util.Optional;

public class PowerLootCondition implements LootItemCondition {
    public static final MapCodec<PowerLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            ResourceLocation.CODEC
                .fieldOf("power")
                .forGetter(c -> c.powerId),
            ResourceLocation.CODEC
                .optionalFieldOf("source", null)
                .forGetter(c -> c.powerSourceId)
        )
            .apply(instance, PowerLootCondition::new)
    );
    public static final LootItemConditionType TYPE = new LootItemConditionType(CODEC);
    private final ResourceLocation powerId;
    private final ResourceLocation powerSourceId;

    private PowerLootCondition(ResourceLocation powerId, ResourceLocation powerSourceId) {
        this.powerId = powerId;
        this.powerSourceId = powerSourceId;
    }

    public LootItemConditionType getType() {
        return TYPE;
    }

    public boolean test(LootContext lootContext) {

        Optional<PowerHolderComponent> optionalPowerHolderComponent = PowerHolderComponent.KEY.maybeGet(
            lootContext.getParamOrNull(LootContextParams.THIS_ENTITY)
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

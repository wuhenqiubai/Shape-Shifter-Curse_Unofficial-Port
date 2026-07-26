package net.onixary.shapeShifterCurseFabric.integration.origins.util;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.onixary.shapeShifterCurseFabric.integration.origins.component.OriginComponent;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.Origin;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.OriginLayer;
import net.onixary.shapeShifterCurseFabric.integration.origins.registry.ModComponents;

import java.util.Map;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class OriginLootCondition implements LootItemCondition {

    public static final MapCodec<OriginLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ResourceLocation.CODEC.fieldOf("origin").forGetter(OriginLootCondition::getOrigin),
        ResourceLocation.CODEC.optionalFieldOf("layer").forGetter(OriginLootCondition::getLayer)
    ).apply(instance, OriginLootCondition::new));

    public static final LootItemConditionType TYPE = new LootItemConditionType(CODEC);

    private final ResourceLocation origin;
    private final Optional<ResourceLocation> layer;

    private OriginLootCondition(ResourceLocation origin, Optional<ResourceLocation> layer) {
        this.origin = origin;
        this.layer = layer;
    }

    @Override
    public LootItemConditionType getType() {
        return TYPE;
    }

    @Override
    public boolean test(LootContext lootContext) {
        OriginComponent component = ModComponents.ORIGIN.maybeGet(lootContext.getParamOrNull(LootContextParams.THIS_ENTITY))
            .orElse(null);
        if (component == null) {
            return false;
        }
        for (Map.Entry<OriginLayer, Origin> entry : component.getOrigins().entrySet()) {
            ResourceLocation layerId = entry.getKey().getIdentifier();
            ResourceLocation originId = entry.getValue().getIdentifier();
            if (layer.map(layerId::equals).orElse(true) && originId.equals(origin)) {
                return true;
            }
        }
        return false;
    }

    public ResourceLocation getOrigin() {
        return origin;
    }

    public Optional<ResourceLocation> getLayer() {
        return layer;
    }

}

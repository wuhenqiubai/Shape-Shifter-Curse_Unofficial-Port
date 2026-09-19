package io.github.apace100.apoli.power.factory.condition.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.LegacyMaterial;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.List;

public class MaterialCondition {

    public static boolean condition(SerializableData.Instance data, BlockInWorld cachedBlockPosition) {
        BlockState blockState = cachedBlockPosition.getState();
        if(data.isPresent("material")) {
            if(data.<LegacyMaterial>get("material").blockStateIsOfMaterial(blockState)) {
                return true;
            }
        }
        if(data.isPresent("materials")) {
            return data.<List<LegacyMaterial>>get("materials").stream().anyMatch(m -> m.blockStateIsOfMaterial(blockState));
        }
        return false;
    }

    public static ConditionFactory<BlockInWorld> getFactory() {
        return new ConditionFactory<>(Apoli.identifier("material"),
            new SerializableData()
                .add("material", ApoliDataTypes.LEGACY_MATERIAL, null)
                .add("materials", ApoliDataTypes.LEGACY_MATERIALS, null),
            MaterialCondition::condition
        );
    }
}

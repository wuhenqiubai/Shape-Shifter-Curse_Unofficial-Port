package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.ApoliClient;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import java.util.function.Predicate;

public class ModifyFluidRenderPower extends Power {

    private final Predicate<BlockInWorld> blockCondition;
    private final Predicate<FluidState> fluidCondition;
    private final FluidState fluidState;

    public ModifyFluidRenderPower(PowerType<?> type, LivingEntity entity, Predicate<BlockInWorld> blockCondition, Predicate<FluidState> fluidCondition, FluidState state) {
        super(type, entity);
        this.blockCondition = blockCondition;
        this.fluidCondition = fluidCondition;
        this.fluidState = state;
    }

    public boolean doesPrevent(LevelReader world, BlockPos pos) {
        BlockInWorld cbp = new BlockInWorld(world, pos, true);
        if(blockCondition == null || blockCondition.test(cbp)) {
            return fluidCondition == null || fluidCondition.test(world.getFluidState(pos));
        }
        return false;
    }

    public FluidState getFluidState() {
        return fluidState;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void onAdded() {
        super.onAdded();
        ApoliClient.shouldReloadWorldRenderer = true;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void onRemoved() {
        super.onRemoved();
        ApoliClient.shouldReloadWorldRenderer = true;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("modify_fluid_render"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("fluid_condition", ApoliDataTypes.FLUID_CONDITION, null)
                .add("fluid", SerializableDataTypes.FLUID),
            data ->
                (type, player) -> new ModifyFluidRenderPower(type, player,
                    (ConditionFactory<BlockInWorld>.Instance)data.get("block_condition"),
                    (ConditionFactory<FluidState>.Instance)data.get("fluid_condition"),
                    ((Fluid)data.get("fluid")).defaultFluidState()));
    }
}

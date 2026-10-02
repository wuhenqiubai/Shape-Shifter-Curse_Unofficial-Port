package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.block.MaterialCondition;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;

import java.util.Collection;
import java.util.List;

public class BlockConditions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ConditionFactory<>(Apoli.identifier("constant"), new SerializableData()
            .add("value", SerializableDataTypes.BOOLEAN),
            (data, block) -> data.getBoolean("value")));
        register(new ConditionFactory<>(Apoli.identifier("and"), new SerializableData()
            .add("conditions", ApoliDataTypes.BLOCK_CONDITIONS),
            (data, block) -> ((List<ConditionFactory<BlockInWorld>.Instance>)data.get("conditions")).stream().allMatch(
                condition -> condition.test(block)
            )));
        register(new ConditionFactory<>(Apoli.identifier("or"), new SerializableData()
            .add("conditions", ApoliDataTypes.BLOCK_CONDITIONS),
            (data, block) -> ((List<ConditionFactory<BlockInWorld>.Instance>)data.get("conditions")).stream().anyMatch(
                condition -> condition.test(block)
            )));
        register(new ConditionFactory<>(Apoli.identifier("offset"), new SerializableData()
            .add("condition", ApoliDataTypes.BLOCK_CONDITION)
            .add("x", SerializableDataTypes.INT, 0)
            .add("y", SerializableDataTypes.INT, 0)
            .add("z", SerializableDataTypes.INT, 0),
            (data, block) -> ((ConditionFactory<BlockInWorld>.Instance)data.get("condition"))
                .test(new BlockInWorld(
                    block.getLevel(),
                    block.getPos().offset(
                        data.getInt("x"),
                        data.getInt("y"),
                        data.getInt("z")
                    ), true))));

        register(new ConditionFactory<>(Apoli.identifier("height"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, block) -> ((Comparison)data.get("comparison")).compare(block.getPos().getY(), data.getInt("compare_to"))));
        DistanceFromCoordinatesConditionRegistry.registerBlockCondition(BlockConditions::register);
        register(new ConditionFactory<>(Apoli.identifier("block"), new SerializableData()
            .add("block", SerializableDataTypes.BLOCK),
            (data, block) -> block.getState().is((Block)data.get("block"))));
        register(new ConditionFactory<>(Apoli.identifier("in_tag"), new SerializableData()
            .add("tag", SerializableDataTypes.BLOCK_TAG),
            (data, block) -> {
                if(block == null || block.getState() == null) {
                    return false;
                }
                return block.getState().is((TagKey<Block>) data.get("tag"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("adjacent"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT)
            .add("adjacent_condition", ApoliDataTypes.BLOCK_CONDITION),
            (data, block) -> {
                ConditionFactory<BlockInWorld>.Instance adjacentCondition = (ConditionFactory<BlockInWorld>.Instance)data.get("adjacent_condition");
                int adjacent = 0;
                for(Direction d : Direction.values()) {
                    if(adjacentCondition.test(new BlockInWorld(block.getLevel(), block.getPos().relative(d), true))) {
                        adjacent++;
                    }
                }
                return ((Comparison)data.get("comparison")).compare(adjacent, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("replacable"), new SerializableData(),
            (data, block) -> block.getState().canBeReplaced()));
        register(new ConditionFactory<>(Apoli.identifier("attachable"), new SerializableData(),
            (data, block) -> {
                for(Direction d : Direction.values()) {
                    BlockPos adjacent = block.getPos().relative(d);
                    if(block.getLevel().getBlockState(adjacent).isFaceSturdy(block.getLevel(), block.getPos(), d.getOpposite())) {
                        return true;
                    }
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("fluid"), new SerializableData()
            .add("fluid_condition", ApoliDataTypes.FLUID_CONDITION),
            (data, block) -> ((ConditionFactory<FluidState>.Instance)data.get("fluid_condition")).test(block.getLevel().getFluidState(block.getPos()))));
        register(new ConditionFactory<>(Apoli.identifier("movement_blocking"), new SerializableData(),
            (data, block) -> block.getState().blocksMotion() && !block.getState().getCollisionShape(block.getLevel(), block.getPos()).isEmpty()));
        register(new ConditionFactory<>(Apoli.identifier("light_blocking"), new SerializableData(),
            (data, block) -> block.getState().canOcclude()));
        register(new ConditionFactory<>(Apoli.identifier("water_loggable"), new SerializableData(),
            (data, block) -> block.getState().getBlock() instanceof LiquidBlockContainer));
        register(new ConditionFactory<>(Apoli.identifier("exposed_to_sky"), new SerializableData(),
            (data, block) -> block.getLevel().canSeeSky(block.getPos())));
        register(new ConditionFactory<>(Apoli.identifier("light_level"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT)
            .add("light_type", SerializableDataType.enumValue(LightLayer.class), null),
            (data, block) -> {
                int value;
                if(data.isPresent("light_type")) {
                    LightLayer lightType = (LightLayer)data.get("light_type");
                    value = block.getLevel().getBrightness(lightType, block.getPos());
                } else {
                    value = block.getLevel().getMaxLocalRawBrightness(block.getPos());
                }
                return ((Comparison)data.get("comparison")).compare(value, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("block_state"), new SerializableData()
            .add("property", SerializableDataTypes.STRING)
            .add("comparison", ApoliDataTypes.COMPARISON, null)
            .add("compare_to", SerializableDataTypes.INT, null)
            .add("value", SerializableDataTypes.BOOLEAN, null)
            .add("enum", SerializableDataTypes.STRING, null),
            (data, block) -> {
                BlockState state = block.getState();
                Collection<Property<?>> properties = state.getProperties();
                String desiredPropertyName = data.getString("property");
                Property<?> property = null;
                for(Property<?> p : properties) {
                    if(p.getName().equals(desiredPropertyName)) {
                        property = p;
                        break;
                    }
                }
                if(property != null) {
                    Object value = state.getValue(property);
                    if(data.isPresent("enum") && value instanceof Enum) {
                        return ((Enum)value).name().equalsIgnoreCase(data.getString("enum"));
                    } else if(data.isPresent("value") && value instanceof Boolean) {
                        return (Boolean) value == data.getBoolean("value");
                    } else if(data.isPresent("comparison") && data.isPresent("compare_to") && value instanceof Integer) {
                        return ((Comparison)data.get("comparison")).compare((Integer) value, data.getInt("compare_to"));
                    }
                    return true;
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("block_entity"), new SerializableData(),
            (data, block) -> block.getEntity() != null));
        register(new ConditionFactory<>(Apoli.identifier("nbt"), new SerializableData()
            .add("nbt", SerializableDataTypes.NBT),
            (data, block) -> {
                CompoundTag nbt = new CompoundTag();
                if(block.getEntity() != null) {
                    nbt = block.getEntity().saveWithFullMetadata(block.getLevel().registryAccess());
                }
                return NbtUtils.compareNbt((CompoundTag)data.get("nbt"), nbt, true);
            }));
        register(new ConditionFactory<>(Apoli.identifier("slipperiness"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
            (data, block) -> {
                BlockState state = block.getState();
                return ((Comparison)data.get("comparison")).compare(state.getBlock().getFriction(), data.getFloat("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("blast_resistance"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
            (data, block) -> {
                BlockState state = block.getState();
                return ((Comparison)data.get("comparison")).compare(state.getBlock().getExplosionResistance(), data.getFloat("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("hardness"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
            (data, block) -> {
                BlockState state = block.getState();
                return ((Comparison)data.get("comparison")).compare(state.getBlock().defaultDestroyTime(), data.getFloat("compare_to"));
            }));
        register(MaterialCondition.getFactory());
    }

    private static void register(ConditionFactory<BlockInWorld> conditionFactory) {
        Registry.register(ApoliRegistries.BLOCK_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}

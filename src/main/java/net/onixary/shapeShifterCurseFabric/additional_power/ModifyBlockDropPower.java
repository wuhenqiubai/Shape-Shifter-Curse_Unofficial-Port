package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ModifyBlockDropPower extends Power {
    private final ConditionFactory<BlockInWorld>.Instance blockCondition;
    private final float chance;
    private final List<ItemStack> targetItemStack;

    public ModifyBlockDropPower(PowerType<?> type, LivingEntity entity, SerializableData.Instance data) {
        super(type, entity);
        this.blockCondition = data.get("block_condition");
        this.chance = data.get("chance");
        this.targetItemStack = Objects.requireNonNullElseGet(data.get("target_item_stack_list"), LinkedList::new);
    }

    public boolean CanApply(BlockInWorld blockPosition) {
        if (this.blockCondition != null) {
            return this.blockCondition.test(blockPosition);
        }
        return true;
    }

    public @Nullable List<ItemStack> Apply(RandomSource randomSource) {
        if (randomSource.nextFloat() < this.chance) {
            return this.targetItemStack;
        }
        return null;
    }

    public static PowerFactory<?> createFactory() {
        return new PowerFactory<>(
                ShapeShifterCurseFabric.identifier("modify_block_drop"),
                new SerializableData()
                        .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                        .add("chance", SerializableDataTypes.FLOAT, 0.0f)
                        .add("target_item_stack_list", SerializableDataTypes.ITEM_STACKS, null),
                data -> (powerType, entity) -> new ModifyBlockDropPower(powerType, entity, data)
        ).allowCondition();
    }
}

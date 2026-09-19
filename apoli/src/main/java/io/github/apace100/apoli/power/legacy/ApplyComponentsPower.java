package io.github.apace100.apoli.power.legacy;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class ApplyComponentsPower extends Power {
    private final Predicate<ItemStack> itemCondition;
    private final EnumSet<EquipmentSlot> slots;
    private final DataComponentPatch components;
    private final boolean replaceExisting;

    public ApplyComponentsPower(
        PowerType<?> type, LivingEntity entity,
        Predicate<ItemStack> itemCondition, EnumSet<EquipmentSlot> slots,
        DataComponentPatch components, boolean replaceExisting
    ) {
        super(type, entity);
        this.itemCondition = itemCondition;
        this.slots = slots;
        this.components = components;
        this.replaceExisting = replaceExisting;
    }

    public Map<EquipmentSlot, ItemStack> getAppliedStacks(LivingEntity entity) {
        var stacks = new HashMap<EquipmentSlot, ItemStack>();
        for (EquipmentSlot slot : this.slots) {
            var stack = entity.getItemBySlot(slot);
            if (!stack.isEmpty() && this.itemCondition.test(stack)) {
                stacks.put(slot, stack);
            }
        }

        return stacks;
    }

    public DataComponentPatch getComponents() {
        return components;
    }

    public boolean shouldReplaceExisting() {
        return replaceExisting;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.legacy("apply_components"),
            new SerializableData()
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION)
                .add("slots", SerializableDataTypes.EQUIPMENT_SLOT_SET)
                .add("components", SerializableDataTypes.DATA_COMPONENTS)
                .add("replace_existing", SerializableDataTypes.BOOLEAN, false),
            data -> (type, player) -> new ApplyComponentsPower(type, player, data.get("item_condition"), data.get("slots"), data.get("components"), data.get("replace_existing"))
        ).allowCondition();
    }
}

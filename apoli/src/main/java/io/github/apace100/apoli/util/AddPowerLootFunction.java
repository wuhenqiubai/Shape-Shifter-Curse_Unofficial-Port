package io.github.apace100.apoli.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class AddPowerLootFunction extends LootItemConditionalFunction {
    public static final MapCodec<AddPowerLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
        LootItemConditionalFunction.commonFields(instance)
            .and(instance.group(
                EquipmentSlot.CODEC
                    .fieldOf("slot")
                    .forGetter(e -> e.slot),
                Identifier.CODEC
                    .fieldOf("power")
                    .forGetter(e -> e.powerId),
                Codec.BOOL
                    .optionalFieldOf("hidden", false)
                    .forGetter(e -> e.hidden),
                Codec.BOOL
                    .optionalFieldOf("negative", false)
                    .forGetter(e -> e.negative)
            ))
            .apply(instance, AddPowerLootFunction::new)
    );

    private final EquipmentSlot slot;
    private final Identifier powerId;
    private final boolean hidden;
    private final boolean negative;

    private AddPowerLootFunction(List<LootItemCondition> conditions, EquipmentSlot slot, Identifier powerId, boolean hidden, boolean negative) {
        super(conditions);
        this.slot = slot;
        this.powerId = powerId;
        this.hidden = hidden;
        this.negative = negative;
    }

    @Override
    public MapCodec<? extends LootItemConditionalFunction> codec() {
        return CODEC;
    }

    public ItemStack run(ItemStack stack, LootContext context) {
        StackPowerUtil.addPower(stack, slot, powerId, hidden, negative);
        return stack;
    }

    public static LootItemConditionalFunction.Builder<?> builder(EquipmentSlot slot, Identifier powerId, boolean hidden, boolean negative) {
        return simpleBuilder((conditions) -> new AddPowerLootFunction(conditions, slot, powerId, hidden, negative));
    }
}

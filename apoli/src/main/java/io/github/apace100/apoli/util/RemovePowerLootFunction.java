package io.github.apace100.apoli.util;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

public class RemovePowerLootFunction extends LootItemConditionalFunction {
    public static final MapCodec<RemovePowerLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
        LootItemConditionalFunction.commonFields(instance)
            .and(instance.group(
                EquipmentSlot.CODEC
                    .fieldOf("slot")
                    .forGetter(e -> e.slot),
                Identifier.CODEC
                    .fieldOf("power")
                    .forGetter(e -> e.powerId)
            ))
            .apply(instance, RemovePowerLootFunction::new)
    );

    private final EquipmentSlot slot;
    private final Identifier powerId;

    private RemovePowerLootFunction(Optional<Holder<LootItemCondition>> condition, EquipmentSlot slot, Identifier powerId) {
        super(condition);
        this.slot = slot;
        this.powerId = powerId;
    }

    @Override
    public MapCodec<? extends LootItemConditionalFunction> codec() {
        return CODEC;
    }

    public ItemStack run(ItemStack stack, LootContext context) {
        StackPowerUtil.removePower(stack, slot, powerId);
        return stack;
    }

    public static net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Builder<?> builder(EquipmentSlot slot, Identifier powerId) {
        return simpleBuilder((conditions) -> new RemovePowerLootFunction(conditions, slot, powerId));
    }

}

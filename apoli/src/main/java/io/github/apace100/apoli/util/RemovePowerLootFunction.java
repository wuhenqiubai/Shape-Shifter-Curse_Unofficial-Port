package io.github.apace100.apoli.util;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class RemovePowerLootFunction extends LootItemConditionalFunction {
    public static final MapCodec<RemovePowerLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
        LootItemConditionalFunction.commonFields(instance)
            .and(instance.group(
                EquipmentSlot.CODEC
                    .fieldOf("slot")
                    .forGetter(e -> e.slot),
                ResourceLocation.CODEC
                    .fieldOf("power")
                    .forGetter(e -> e.powerId)
            ))
            .apply(instance, RemovePowerLootFunction::new)
    );

    public static final LootItemFunctionType<RemovePowerLootFunction> TYPE = new LootItemFunctionType<>(CODEC);

    private final EquipmentSlot slot;
    private final ResourceLocation powerId;

    private RemovePowerLootFunction(List<LootItemCondition> conditions, EquipmentSlot slot, ResourceLocation powerId) {
        super(conditions);
        this.slot = slot;
        this.powerId = powerId;
    }

    public LootItemFunctionType getType() {
        return TYPE;
    }

    public ItemStack run(ItemStack stack, LootContext context) {
        StackPowerUtil.removePower(stack, slot, powerId);
        return stack;
    }

    public static net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Builder<?> builder(EquipmentSlot slot, ResourceLocation powerId) {
        return simpleBuilder((conditions) -> new RemovePowerLootFunction(conditions, slot, powerId));
    }

}

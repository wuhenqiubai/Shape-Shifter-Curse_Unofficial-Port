package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.item.EnchantmentCondition;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.apoli.util.StackPowerUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.util.UpgradeUtils;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ItemConditions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ConditionFactory<>(Apoli.identifier("constant"), new SerializableData()
            .add("value", SerializableDataTypes.BOOLEAN),
            (data, stack) -> data.getBoolean("value")));
        register(new ConditionFactory<>(Apoli.identifier("and"), new SerializableData()
            .add("conditions", ApoliDataTypes.ITEM_CONDITIONS),
            (data, stack) -> ((List<ConditionFactory<ItemStack>.Instance>)data.get("conditions")).stream().allMatch(
                condition -> condition.test(stack)
            )));
        register(new ConditionFactory<>(Apoli.identifier("or"), new SerializableData()
            .add("conditions", ApoliDataTypes.ITEM_CONDITIONS),
            (data, stack) -> ((List<ConditionFactory<ItemStack>.Instance>)data.get("conditions")).stream().anyMatch(
                condition -> condition.test(stack)
            )));
        register(new ConditionFactory<>(Apoli.identifier("food"), new SerializableData(),
            (data, stack) -> stack.has(DataComponents.CONSUMABLE) && stack.has(DataComponents.FOOD)));
        register(new ConditionFactory<>(Apoli.identifier("ingredient"), new SerializableData()
            .add("ingredient", SerializableDataTypes.INGREDIENT),
            (data, stack) -> ((Ingredient)data.get("ingredient")).test(stack)));
        register(new ConditionFactory<>(Apoli.identifier("armor_value"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, stack) -> {
                double armor = 0;
                if(stack.has(DataComponents.ATTRIBUTE_MODIFIERS)) {
                    var modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
                    for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
                        if (entry.attribute() == Attributes.ARMOR) {
                            double modifier = entry.modifier().amount();

                            armor += switch (entry.modifier().operation()) {
                                case ADD_VALUE -> modifier;
                                case ADD_MULTIPLIED_BASE -> modifier * 0.0;
                                case ADD_MULTIPLIED_TOTAL -> modifier * armor;
                            };
                        }
                    }
                }
                return ((Comparison)data.get("comparison")).compare((int) armor, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("harvest_level"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, stack) -> {
                int harvestLevel = 0;
                if(stack.has(DataComponents.TOOL)) {
                    var tool = stack.get(DataComponents.TOOL);
                    for (Tool.Rule rule : tool.rules()) {
                        if (rule.blocks() instanceof HolderSet.Named<Block> named) {
                            var tag = named.key();

                            if (harvestLevel < 4 && tag.equals(BlockTags.NEEDS_DIAMOND_TOOL)) {
                                harvestLevel = 4;
                            } else if (harvestLevel < 3 && tag.equals(BlockTags.NEEDS_IRON_TOOL)) {
                                harvestLevel = 3;
                            } else if (harvestLevel < 1 && tag.equals(BlockTags.NEEDS_STONE_TOOL)) {
                                harvestLevel = 1;
                            }
                        }
                    }
                }
                return ((Comparison)data.get("comparison")).compare(harvestLevel, data.getInt("compare_to"));
            }));
        register(EnchantmentCondition.getFactory());
        register(new ConditionFactory<>(Apoli.identifier("meat"), new SerializableData(),
            (data, stack) -> stack.has(DataComponents.CONSUMABLE) && stack.is(ItemTags.MEAT)));
        register(new ConditionFactory<>(Apoli.identifier("nbt"), new SerializableData()
            .add("nbt", SerializableDataTypes.NBT), (data, stack) -> {
            if (stack.isEmpty())
                return false;

            CompoundTag oldTag = data.get("nbt");
            CompoundTag recreatedTag = new CompoundTag();
            recreatedTag.putString("id", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            recreatedTag.putInt("Count", stack.getCount());
            recreatedTag.put("tag", oldTag);

            var convertedStackNbt = UpgradeUtils.upgradeStack(recreatedTag);
            var convertedStack = ItemStack.OPTIONAL_CODEC.decode(NbtOps.INSTANCE, convertedStackNbt).getOrThrow().getFirst();

            return convertedStack.getComponentsPatch().equals(stack.getComponentsPatch());
        }));
        register(new ConditionFactory<>(Apoli.identifier("fireproof"), new SerializableData(),
            (data, stack) -> stack.has(DataComponents.DAMAGE_RESISTANT) && stack.get(DataComponents.DAMAGE_RESISTANT).types().equals(DamageTypeTags.IS_FIRE)));
        register(new ConditionFactory<>(Apoli.identifier("enchantable"), new SerializableData(),
            (data, stack) -> !stack.isEnchantable()));
        register(new ConditionFactory<>(Apoli.identifier("power_count"), new SerializableData()
            .add("slot", SerializableDataTypes.EQUIPMENT_SLOT, null)
            .add("compare_to", SerializableDataTypes.INT)
            .add("comparison", ApoliDataTypes.COMPARISON),
            (data, stack) -> {
                int totalCount = 0;
                if(data.isPresent("slot")) {
                    totalCount = StackPowerUtil.getPowers(stack, data.get("slot")).size();
                } else {
                    for (EquipmentSlot slot :
                        EquipmentSlot.values()) {
                        totalCount += StackPowerUtil.getPowers(stack, slot).size();
                    }
                }
                return ((Comparison)data.get("comparison")).compare(totalCount, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("has_power"), new SerializableData()
            .add("slot", SerializableDataTypes.EQUIPMENT_SLOT, null)
            .add("power", SerializableDataTypes.IDENTIFIER),
            (data, stack) -> {
                Identifier power = data.getId("power");
                if(data.isPresent("slot")) {
                    return StackPowerUtil.getPowers(stack, data.get("slot")).stream().anyMatch(p -> p.powerId.equals(power));
                } else {
                    for (EquipmentSlot slot :
                        EquipmentSlot.values()) {
                        if(StackPowerUtil.getPowers(stack, slot).stream().anyMatch(p -> p.powerId.equals(power))) {
                            return true;
                        }
                    }
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("empty"), new SerializableData(),
            (data, stack) -> stack.isEmpty()));
        register(new ConditionFactory<>(Apoli.identifier("amount"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, stack) -> ((Comparison)data.get("comparison")).compare(stack.getCount(), data.getInt("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("is_damageable"), new SerializableData(),
            (data, stack) -> stack.isDamageableItem()));
        register(new ConditionFactory<>(Apoli.identifier("durability"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, stack) -> ((Comparison)data.get("comparison")).compare(stack.getMaxDamage() - stack.getDamageValue(), data.getInt("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("relative_durability"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
            (data, stack) -> ((Comparison)data.get("comparison")).compare((float)(stack.getMaxDamage() - stack.getDamageValue()) / stack.getMaxDamage(), data.getFloat("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("is_equippable"), new SerializableData()
            .add("equipment_slot", SerializableDataTypes.EQUIPMENT_SLOT),
            (data, stack) -> {
                if (!stack.has(DataComponents.EQUIPPABLE))
                    return false;
                var equippable = stack.get(DataComponents.EQUIPPABLE);
                return equippable.slot() == data.get("equipment_slot");
            }));

        // Apoli: Legacy implementations
        register(new ConditionFactory<>(Apoli.legacy("components"), new SerializableData()
            .add("components", SerializableDataTypes.DATA_COMPONENTS),
            (data, stack) -> {
                if (stack.isEmpty())
                    return false;

                DataComponentPatch components = data.get("components");

                for (Map.Entry<DataComponentType<?>, Optional<?>> entry : components.entrySet()) {
                    var type = entry.getKey();
                    var value = entry.getValue();

                    var stackValue = stack.get(type);
                    if (stackValue != null && value.isPresent() && stackValue != value.orElseThrow())
                        return false;

                    if (stackValue == null && value.isPresent())
                        return false;

                    if (stackValue != null && value.isEmpty())
                        return false;
                }

                return true;
            }
        ));
    }

    private static void register(ConditionFactory<ItemStack> conditionFactory) {
        Registry.register(ApoliRegistries.ITEM_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}

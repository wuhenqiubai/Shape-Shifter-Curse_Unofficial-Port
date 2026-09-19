package io.github.apace100.apoli.power.factory.condition.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class EnchantmentCondition {

    public static boolean condition(SerializableData.Instance data, ItemStack stack) {

        ResourceKey<Enchantment> enchantment = data.get("enchantment");
        Comparison comparison = data.get("comparison");
        int compareTo = data.get("compare_to");

        if (!stack.has(DataComponents.ENCHANTMENTS))
            return comparison.compare(0, compareTo);

        if (enchantment != null) {
            var ench = stack.get(DataComponents.ENCHANTMENTS).keySet().stream().filter(e -> e.unwrapKey().orElseThrow().equals(enchantment)).findFirst().orElse(null);
            return comparison.compare(EnchantmentHelper.getItemEnchantmentLevel(ench, stack), compareTo);
        }
        else return comparison.compare(stack.get(DataComponents.ENCHANTMENTS).size(), compareTo);

    }

    public static ConditionFactory<ItemStack> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("enchantment"),
            new SerializableData()
                .add("enchantment", SerializableDataTypes.ENCHANTMENT, null)
                .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN)
                .add("compare_to", SerializableDataTypes.INT, 0),
            EnchantmentCondition::condition
        );
    }

}

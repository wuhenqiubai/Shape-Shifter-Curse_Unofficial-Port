package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.util.Pair;
import net.minecraft.world.World;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

public class AdditionalItemCondition {
    public static void register() {
        register(IsMorphScaleItemCondition.getFactory1());
        register(IsMorphScaleItemCondition.getFactory2());
        register(new ConditionFactory<Pair<World, ItemStack>>(
                ShapeShifterCurseFabric.identifier("is_weapon"),
                new SerializableData(),
                (data, pair) -> {
                    ItemStack itemstack = pair.getRight();
                    var attrComponent = itemstack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
                    double totalAdd = 0;
                    if (attrComponent != null) {
                        for (var entry : attrComponent.modifiers()) {
                            if (entry.slot().matches(EquipmentSlot.MAINHAND)
                                    && entry.attribute() == EntityAttributes.GENERIC_ATTACK_DAMAGE
                                    && entry.modifier().operation() == EntityAttributeModifier.Operation.ADD_VALUE) {
                                totalAdd += entry.modifier().value();
                            }
                        }
                    }
                    return false;
                }
        ));
        FoodUtilsCondition.registerCondition(AdditionalItemCondition::register);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void register(ConditionFactory<?> conditionFactory) {
        Registry.register(ApoliRegistries.ITEM_CONDITION, conditionFactory.getSerializerId(), (ConditionFactory) conditionFactory);
    }
}

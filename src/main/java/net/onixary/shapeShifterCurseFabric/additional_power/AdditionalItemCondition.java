package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

public class AdditionalItemCondition {
    public static void register() {
        register(IsMorphScaleItemCondition.getFactory1());
        register(IsMorphScaleItemCondition.getFactory2());
        register(new ConditionFactory<Tuple<Level, ItemStack>>(
                ShapeShifterCurseFabric.identifier("is_weapon"),
                new SerializableData(),
                (data, pair) -> {
//                (data, itemstack) -> {
//                    Collection<EntityAttributeModifier> modifiers = itemstack.getItem().getAttributeModifiers(EquipmentSlot.MAINHAND).get(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                    ItemStack itemstack = pair.getB();
                    var attrComponent = itemstack.get(DataComponents.ATTRIBUTE_MODIFIERS);
                    double totalAdd = 0;
                    if (attrComponent != null) {
                        for (var entry : attrComponent.modifiers()) {
                            if (entry.slot().test(EquipmentSlot.MAINHAND)
                                    && entry.attribute() == Attributes.ATTACK_DAMAGE
                                    && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                                totalAdd += entry.modifier().amount();
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

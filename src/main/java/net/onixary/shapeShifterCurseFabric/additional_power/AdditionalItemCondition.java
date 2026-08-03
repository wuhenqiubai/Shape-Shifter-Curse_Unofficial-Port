package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

public class AdditionalItemCondition {
    public static void register() {
        register(IsMorphScaleItemCondition.getFactory1());
        register(IsMorphScaleItemCondition.getFactory2());
        // 1.21.11 修复：is_weapon 注册为 ITEM_CONDITION（checkInventory 里 itemCondition.test(ItemStack)），
        // 泛型必须 ItemStack；原 Tuple<Level, ItemStack> 会在 pair.getB() 处运行时 ClassCastException 崩服。
        register(new ConditionFactory<ItemStack>(
                ShapeShifterCurseFabric.identifier("is_weapon"),
                new SerializableData(),
                (data, itemstack) -> {
//                (data, itemstack) -> {
//                    Collection<EntityAttributeModifier> modifiers = itemstack.getItem().getAttributeModifiers(EquipmentSlot.MAINHAND).get(EntityAttributes.GENERIC_ATTACK_DAMAGE);
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
                    // 1.21.11 顺手修复：is_weapon 原本计算了 totalAdd 却 return false（永远返回 false，导致"裸手"条件永远成立）。
                    // 改为按主手攻击力判断（有 ADD_VALUE 攻击力加成即视为武器）。
                    return totalAdd > 0;
                }
        ));
        FoodUtilsCondition.registerCondition(AdditionalItemCondition::register);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void register(ConditionFactory<?> conditionFactory) {
        Registry.register(ApoliRegistries.ITEM_CONDITION, conditionFactory.getSerializerId(), (ConditionFactory) conditionFactory);
    }
}

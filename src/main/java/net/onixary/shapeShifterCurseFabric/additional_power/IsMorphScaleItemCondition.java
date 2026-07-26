package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.util.ModTags;

public class IsMorphScaleItemCondition {
    public static final String IsMorphScaleArmorTagName = "MorphScaleItem";
    public static final String IsMorphScaleFoodTagName = "MorphScaleFood";  // TODO 得改一下名称 我想不出名字了

    public static boolean MSI_condition(SerializableData.Instance data, ItemStack itemStack) {
        if (itemStack.is(ModTags.MorphScaleItem_Tag)) {
            return true;
        }
        var customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData != null && customData.copyTag().getBoolean(IsMorphScaleArmorTagName)) {
            return true;
        }
        return false;
    }

    public static boolean MSF_condition(SerializableData.Instance data, ItemStack itemStack) {
        if (!ShapeShifterCurseFabric.commonConfig.enableFoodHabitSystem) {
            return true;
        }
        if (itemStack.is(ModTags.MorphScaleItem_Tag)) {
            return true;
        }
        var customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag itemNBT = customData.copyTag();
            if (itemNBT.getBoolean(IsMorphScaleFoodTagName)) {
                return true;
            }
            if (itemNBT.getBoolean(IsMorphScaleArmorTagName)) {
                return true;
            }
        }
        return false;
    }

    public static ConditionFactory<ItemStack> getFactory1() {
        return new ConditionFactory<ItemStack>(
            ShapeShifterCurseFabric.identifier("is_morph_scale_item"),
            new SerializableData(),
            IsMorphScaleItemCondition::MSI_condition
        );
    }
    public static ConditionFactory<ItemStack> getFactory2() {
        return new ConditionFactory<ItemStack>(
            ShapeShifterCurseFabric.identifier("is_morph_scale_food"),
            new SerializableData(),
            IsMorphScaleItemCondition::MSF_condition
        );
    }
}

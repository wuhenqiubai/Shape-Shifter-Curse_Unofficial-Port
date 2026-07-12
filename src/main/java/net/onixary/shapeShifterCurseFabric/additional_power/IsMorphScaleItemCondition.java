package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Pair;
import net.minecraft.world.World;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.util.ModTags;

public class IsMorphScaleItemCondition {
    public static final String IsMorphScaleArmorTagName = "MorphScaleItem";
    public static final String IsMorphScaleFoodTagName = "MorphScaleFood";

	public static boolean MSI_condition(SerializableData.Instance data, Pair<World, ItemStack> pair) {
		ItemStack itemStack = pair.getRight();
        if (itemStack.isIn(ModTags.MorphScaleItem_Tag)) {
            return true;
        }
        var customData = itemStack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData != null) {
	        return customData.copyNbt().getBoolean(IsMorphScaleArmorTagName);
        }
        return false;
    }

	public static boolean MSF_condition(SerializableData.Instance data, Pair<World, ItemStack> pair) {
		ItemStack itemStack = pair.getRight();
		if (!ShapeShifterCurseFabric.commonConfig.enableFoodHabitSystem) {
			return true;
		}
		if (itemStack.isIn(ModTags.MorphScaleItem_Tag)) {
			return true;
		}
		var customData = itemStack.get(DataComponentTypes.CUSTOM_DATA);
		if (customData != null) {
			NbtCompound itemNBT = customData.copyNbt();
			if (itemNBT.getBoolean(IsMorphScaleFoodTagName)) {
				return true;
			}
			return itemNBT.getBoolean(IsMorphScaleArmorTagName);
		}
		return false;
	}

	public static ConditionFactory<Pair<World, ItemStack>> getFactory1() {
		return new ConditionFactory<>(
				ShapeShifterCurseFabric.identifier("is_morph_scale_item"),
				new SerializableData(),
				IsMorphScaleItemCondition::MSI_condition
		);
    }

	public static ConditionFactory<Pair<World, ItemStack>> getFactory2() {
		return new ConditionFactory<>(
				ShapeShifterCurseFabric.identifier("is_morph_scale_food"),
				new SerializableData(),
				IsMorphScaleItemCondition::MSF_condition
		);
    }
}
